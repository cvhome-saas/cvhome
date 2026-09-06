import {Injectable, inject} from '@angular/core';
import {Observable, map, switchMap} from 'rxjs';

import {CrudService} from '@cvhome-saas/ui-kit';
import type {PageRequest, PageT} from '@cvhome-saas/ui-kit';
import type {
  OrderStatus,
  PersistableOrderStatusHistory,
  ReadableCountry,
  ReadableOrder,
  ReadableOrderStatusHistory,
} from '@models/checkout';

/** The filters `OrderApi.list` accepts. All optional; omitted means unfiltered. */
export interface OrderQuery extends PageRequest {
  readonly name?: string;
  readonly id?: number;
  readonly status?: OrderStatus;
  readonly phone?: string;
  readonly email?: string;
  /**
   * The exact join onto a customer.
   *
   * `email` is a LIKE, so it also matches an address the term is a substring of; `customerId` is an
   * equality on the column `OrderServiceImpl` writes at placement. It was honoured by
   * `OrderRepository` all along and bound to no request parameter until the customers module.
   */
  readonly customerId?: number;
  /** The exact `orderRef` — the value a payment transaction carries as `requestRef`. */
  readonly ref?: string;
}

const CHECKOUT_API_BASE = '/spg/checkout/api/v1';

/**
 * Orders, as the seller console reads them.
 *
 * **Three of seller-core's methods are deliberately not ported**, because the endpoints they call do
 * not exist anywhere in checkout:
 *
 * - `refundOrder` → `POST …/orders/{id}/refund`
 * - `captureOrder` → `POST …/orders/{id}/capture`
 * - `updateOrder` → `PATCH …/orders/{id}/customer`
 *
 * seller-ui's Refund and Capture buttons and its editable address panels call them and have always
 * 404'd. See lessons.md, "Orders — no refund and no capture" and "Orders — order addresses cannot be
 * edited". The same class of dead code as `ManagerStoreService.create()` in Module 2.
 */
@Injectable({providedIn: 'root'})
export class OrdersService {
  private readonly crudService = inject(CrudService);

  /**
   * A page of orders.
   *
   * The envelope carries **whole orders** — `ReadableOrderList extends ReadableList<ReadableOrder>` —
   * so items, totals, billing and customer are all present without a second call per row. That is why
   * the table can show an item count and a total without N requests.
   *
   * Note `count`, not `size`: `ServletWebConfig` renames Spring's page-size parameter platform-wide.
   */
  list(query: OrderQuery): Observable<PageT<ReadableOrder>> {
    return this.crudService.get(`${CHECKOUT_API_BASE}/private/orders`, {...query});
  }

  /**
   * The one order behind an `orderRef`, or an error when the store has none — what a payments row
   * needs, since payment holds the ref and not the id. Two reads: the list filtered by `ref` finds
   * the id, and only the detail endpoint carries `products` and `customer`.
   */
  byRef(ref: string): Observable<ReadableOrder> {
    return this.list({ref, page: 0, count: 1}).pipe(
      map((page) => {
        const id = page.content[0]?.id;
        if (id === undefined) {
          throw new Error(`No order with ref ${ref}`);
        }
        return id;
      }),
      switchMap((id) => this.get(id)),
    );
  }

  get(orderId: number | string): Observable<ReadableOrder> {
    return this.crudService.get(`${CHECKOUT_API_BASE}/private/orders/${orderId}`);
  }

  /** The status timeline, oldest first as the server returns it. */
  history(orderId: number | string): Observable<ReadableOrderStatusHistory[]> {
    return this.crudService.get(`${CHECKOUT_API_BASE}/private/orders/${orderId}/history`);
  }

  /**
   * Moves the order to a new status, with a comment.
   *
   * This is the only write the console has against an order. Everything else the design asks for —
   * refund, capture, cancel, editing an address — has no endpoint.
   */
  addHistory(orderId: number | string, entry: PersistableOrderStatusHistory): Observable<void> {
    return this.crudService.post(`${CHECKOUT_API_BASE}/private/orders/${orderId}/history`, entry);
  }

  /** Reference data. Orders carry ISO codes; these are what turn `DE` into `Germany`. */
  countries(): Observable<ReadableCountry[]> {
    return this.crudService.get(`${CHECKOUT_API_BASE}/country`);
  }
}
