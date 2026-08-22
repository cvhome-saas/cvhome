import {Injectable, inject} from '@angular/core';
import {Observable, forkJoin, map} from 'rxjs';

import {CustomersService, type CustomerQuery} from '@api/customers/customers.service';
import {OrdersService} from '@api/orders/orders.service';
import {optionalList} from '@core/http/optional';
import type {PageRequest} from '@core/table/table.types';
import type {CustomerAddress, ReadableCustomer, ReadableOrder} from '@models/checkout';
import type {
  CustomerAddressView,
  CustomerOrderRow,
  CustomerOrdersSnapshot,
  CustomerRow,
} from '@models/customers';

/** How many of a customer's orders the dialog lists before deferring to the orders page. */
export const ORDERS_IN_DIALOG = 5;

/** What the page asks the list for: a page, and the one search term. */
export interface CustomersQuery {
  readonly page: PageRequest;
  readonly search: string;
}

/** Everything the table renders for one query. */
export interface CustomersSnapshot {
  readonly rows: readonly CustomerRow[];
  readonly totalElements: number;
  readonly totalPages: number;
}

/**
 * The customers page's data.
 *
 * **One list, and a second read only once someone opens a customer.** There is no per-customer
 * aggregate anywhere on the platform, so a table column showing each row's order count or spend
 * would be one request per row for figures that do not exist as a query — see lessons.md,
 * "Customers — no lifetime or per-customer aggregate". The orders a customer placed are therefore
 * fetched for the one customer being looked at, not for the twenty on screen.
 *
 * **The country lookup is the same one order details uses**, so a customer's country reads the same
 * on both screens. It is optional: an address that shows `PL` instead of `Poland` is still an
 * address, and losing the whole table to the reference endpoint would be a bad trade.
 */
@Injectable({providedIn: 'root'})
export class CustomersApi {
  private readonly customers = inject(CustomersService);
  private readonly orders = inject(OrdersService);

  loadCustomers(query: CustomersQuery): Observable<CustomersSnapshot> {
    return forkJoin({
      page: this.customers.list(toCustomerQuery(query)),
      // Optional: this only turns an ISO code into a country name.
      countries: this.orders.countries().pipe(optionalList()),
    }).pipe(
      map(({page, countries}): CustomersSnapshot => {
        const names = new Map(
          countries.filter((country) => country.code).map((country) => [country.code!, country.name ?? country.code!]),
        );
        return {
          rows: (page.content ?? []).map((customer) => toRow(customer, names)),
          totalElements: page.totalElements,
          totalPages: page.totalPages,
        };
      }),
    );
  }

  /**
   * One customer's orders, and how many there are in total.
   *
   * Joined on `customerId` rather than on the email. Both reach the same endpoint, but `email` is a
   * LIKE — `marta@nordwerk.pl` also matches `xmarta@nordwerk.pl` — while `customerId` is an equality
   * on the column checkout writes at placement. The parameter existed in `OrderCriteria` and was
   * bound to no request parameter until this module.
   */
  loadOrders(customerId: number): Observable<CustomerOrdersSnapshot> {
    return this.orders.list({customerId, page: 0, count: ORDERS_IN_DIALOG}).pipe(
      map((page): CustomerOrdersSnapshot => ({
        rows: (page.content ?? []).map(toOrderRow),
        // The exact count, not `rows.length`: the panel shows the most recent few of all of them.
        totalElements: page.totalElements,
      })),
    );
  }
}

/* --------------------------------------------------------------------------- shaping ---- */

/**
 * The page's filter, as the endpoint's parameters.
 *
 * One box, one parameter. `CustomerApi.list` ANDs whatever it is given and its `name` already spans
 * the billing first name, the billing last name and the email address, so — unlike the orders page,
 * which has to route its term to one of three fields by shape — there is nothing to choose here.
 */
function toCustomerQuery(query: CustomersQuery): CustomerQuery {
  const term = query.search.trim();
  return {
    page: query.page.page,
    count: query.page.count,
    ...(term ? {name: term} : {}),
  };
}

/**
 * One customer, as a table row.
 *
 * TODO(lessons.md): a customer's own name — the populator maps the billing address's instead. See
 * lessons.md, "Customers — a customer's name comes from the billing address".
 *
 * `name` is left empty when there is none. `ReadableCustomerPopulator` sets `firstName` and
 * `lastName` from the **billing address** and never maps the customer's own columns, so a customer
 * who has not checked out has no name to show and the console does not invent one from the email —
 * that would put a value under a label the record does not hold. The email is rendered beside it
 * either way, so a nameless row still identifies itself. See lessons.md, "Customers — a customer's
 * name comes from the billing address".
 */
function toRow(customer: ReadableCustomer, countries: ReadonlyMap<string, string>): CustomerRow {
  const email = customer.emailAddress ?? '';
  const name = [customer.firstName, customer.lastName].filter(Boolean).join(' ').trim();
  const billing = customer.billing;
  const country = countryName(billing?.country, countries);

  return {
    id: customer.id ?? 0,
    name,
    email,
    userName: customer.username ?? '',
    company: billing?.company ?? '',
    phone: billing?.phone ?? '',
    location: [billing?.city, country].filter(Boolean).join(', '),
    initials: initialsOf(name, email),
    addresses: [
      toAddress('billing', customer.billing, countries),
      toAddress('delivery', customer.delivery, countries),
    ].filter((address): address is CustomerAddressView => address !== null),
  };
}

/**
 * One of the two address slots, or `null` when it is empty.
 *
 * Filtered out rather than rendered as a heading over nothing: a customer who has only ever had one
 * address shows one, and the dialog says so in its empty state when both are missing.
 */
function toAddress(
  kind: 'billing' | 'delivery',
  address: CustomerAddress | undefined,
  countries: ReadonlyMap<string, string>,
): CustomerAddressView | null {
  if (!address) {
    return null;
  }

  const lines = [
    address.address,
    [address.postalCode, address.city].filter(Boolean).join(' ').trim(),
    address.stateProvince,
    countryName(address.country, countries),
  ]
    .map((line) => (line ?? '').trim())
    .filter((line) => line !== '');

  const name = [address.firstName, address.lastName].filter(Boolean).join(' ').trim();
  if (!lines.length && !name && !address.company && !address.phone) {
    return null;
  }

  return {kind, name, company: address.company ?? '', phone: address.phone ?? '', lines};
}

/** One order, as the dialog's activity panel reads it. Amounts stay server-formatted. */
function toOrderRow(order: ReadableOrder): CustomerOrderRow {
  return {
    id: order.id ?? 0,
    status: order.orderStatus,
    datePurchased: order.datePurchased ?? '',
    total: order.total?.text ?? '',
  };
}

/** The ISO code turned into a name, or the code when the lookup is missing or failed. */
function countryName(code: string | undefined, countries: ReadonlyMap<string, string>): string {
  if (!code) {
    return '';
  }
  return countries.get(code) ?? code;
}

/**
 * The avatar monogram.
 *
 * Falls back to the email where there is no name — an avatar is a visual anchor in a list, not a
 * name field, so deriving it from whatever identifies the row is not the same as inventing a name.
 */
function initialsOf(name: string, email: string): string {
  const source = name || email;
  if (!source) {
    return '?';
  }
  const words = source.split(/[\s@._-]+/).filter(Boolean);
  return words
    .slice(0, 2)
    .map((word) => word[0]!.toUpperCase())
    .join('');
}
