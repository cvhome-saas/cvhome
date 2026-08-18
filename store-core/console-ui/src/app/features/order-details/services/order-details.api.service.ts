import {Injectable, inject} from '@angular/core';
import {Observable, catchError, forkJoin, map, of} from 'rxjs';

import {OrdersService} from '@api/orders/orders.service';
import {ManagerStoreService} from '@api/tenancy/manager-store.service';
import type {MerchantStore} from '@models/merchant';
import type {
  OrderStatus,
  PersistableOrderStatusHistory,
  ReadableOrder,
  ReadableOrderStatusHistory,
} from '@models/checkout';

/** One order, its timeline, and the country names its address codes resolve to. */
export interface OrderDetail {
  readonly order: ReadableOrder;
  readonly history: readonly ReadableOrderStatusHistory[];
  /** ISO code → country name, for the address panels. */
  readonly countries: ReadonlyMap<string, string>;
  /**
   * The selling store, for the invoice's letterhead. Null when it could not be read — the invoice
   * then prints the store's name alone rather than failing, because an order is still an order when
   * the merchant service is down.
   */
  readonly seller: MerchantStore | null;
}

/**
 * The order detail screen's data.
 *
 * Four calls. Three are required — an order with no timeline is a half-rendered page, and the
 * country lookup is what turns `DE` into `Germany` in both address panels. The fourth, the selling
 * store, backs the invoice's letterhead only, so it is allowed to fail without taking the page.
 *
 * Only one write exists — `addHistory`. The design also asks for refund, capture, cancel, duplicate,
 * shipment creation and address editing; none of them is mapped in checkout. See lessons.md.
 */
@Injectable({providedIn: 'root'})
export class OrderDetailsApi {
  private readonly orders = inject(OrdersService);
  private readonly stores = inject(ManagerStoreService);

  load(orderId: number, storeId: string): Observable<OrderDetail> {
    return forkJoin({
      order: this.orders.get(orderId),
      history: this.orders.history(orderId),
      countries: this.orders.countries(),
      // Optional, unlike the other three: the letterhead is the only thing that reads it, and an
      // order is still an order when the merchant service cannot be reached.
      seller: this.stores.getStoreDetail(storeId).pipe(catchError(() => of(null))),
    }).pipe(
      map(({order, history, countries, seller}) => ({
        order,
        seller,
        // Oldest first is how a timeline reads; the server's order is not guaranteed.
        history: [...history].sort(byDate),
        countries: new Map(
          countries.filter((country) => country.code).map((country) => [country.code!, country.name ?? country.code!]),
        ),
      })),
    );
  }

  /** Moves the order to a new status, with a comment. The only write this screen has. */
  addStatus(orderId: number, status: OrderStatus, comments: string): Observable<void> {
    const entry: PersistableOrderStatusHistory = {
      orderId,
      orderStatus: status,
      comments: comments.trim() || undefined,
      date: new Date().toISOString(),
    };
    return this.orders.addHistory(orderId, entry);
  }
}

function byDate(a: ReadableOrderStatusHistory, b: ReadableOrderStatusHistory): number {
  return new Date(a.date ?? 0).getTime() - new Date(b.date ?? 0).getTime();
}
