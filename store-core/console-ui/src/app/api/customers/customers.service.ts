import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {PageT} from '@core/table/table.types';
import type {ReadableCustomer} from '@models/checkout';

/**
 * The filters `CustomerApi.list` accepts. All optional; omitted means unfiltered.
 *
 * `name` is the one a search box sends: it matches the billing first name, the billing last name
 * **or** the email address. The other four narrow a single field each and are AND-ed with it and
 * with each other, which is why a caller with one box sends `name` alone — `name` and `email`
 * together would match nothing.
 */
export interface CustomerQuery {
  readonly page: number;
  readonly count: number;
  readonly name?: string;
  readonly firstName?: string;
  readonly lastName?: string;
  readonly email?: string;
  /** The billing country's ISO code. */
  readonly country?: string;
}

export const CUSTOMER_API_BASE = '/spg/checkout/api/v1';

/**
 * Customers, as the seller console reads them. **One endpoint, and it is read-only.**
 *
 * TODO(lessons.md): reading one customer by id — no backend endpoint. See lessons.md, "Customers —
 * no customer detail endpoint". When it lands this gains a `get(id)` and the page stops depending
 * on the row being in the page it already loaded.
 *
 * There is no `GET …/private/customers/{id}`: `CustomerFacade.getCustomerById` exists in checkout
 * and is exposed by no controller, and the one detail endpoint — `/private/customer/info` — is
 * guarded by `STORE-POD.CUSTOMER.*`, which `CustomPermissionEvaluator` resolves to
 * `isCustomerInSameStore`, a **shopper** token. A seller JWT is refused by it. That is why the
 * page reads its detail out of a row it already has rather than fetching one, and why a link into
 * this page carries a search term rather than an id. See lessons.md, "Customers — no customer
 * detail endpoint".
 *
 * There is no create, update or delete either, and nothing on the platform sends mail.
 */
@Injectable({providedIn: 'root'})
export class CustomersService {
  private readonly crudService = inject(CrudService);

  /**
   * A page of the open store's customers.
   *
   * `count`, not Spring's `size`: `checkout-service` depends on `store-commons:autoconfigure`,
   * whose `ServletWebConfig` registers a `PageableHandlerMethodArgumentResolver` with
   * `setSizeParameterName("count")`.
   *
   * `?store=` is stamped by `CrudService` from the request context, so it is never passed here —
   * seller-core threaded it through every call site by hand.
   *
   * The filters below were implemented by `CustomerRepository.findByStoreMerchantId` from the
   * beginning and bound to no request parameter, so seller-ui's list could not search at all.
   */
  list(query: CustomerQuery): Observable<PageT<ReadableCustomer>> {
    return this.crudService.get(`${CUSTOMER_API_BASE}/private/customers`, {...query});
  }
}
