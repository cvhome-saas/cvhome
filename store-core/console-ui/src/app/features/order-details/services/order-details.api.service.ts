import {Injectable, inject} from '@angular/core';
import {Observable, forkJoin, map} from 'rxjs';

import {OrdersService} from '@api/orders/orders.service';
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
}

/**
 * The order detail screen's data.
 *
 * Three calls, all required: an order with no timeline is a half-rendered page, and the country
 * lookup is what turns `DE` into `Germany` in both address panels.
 *
 * Only one write exists — `addHistory`. The design also asks for refund, capture, cancel, duplicate,
 * shipment creation and address editing; none of them is mapped in checkout. See lessons.md.
 */
@Injectable({providedIn: 'root'})
export class OrderDetailsApi {
  private readonly orders = inject(OrdersService);

  load(orderId: number): Observable<OrderDetail> {
    return forkJoin({
      order: this.orders.get(orderId),
      history: this.orders.history(orderId),
      countries: this.orders.countries(),
    }).pipe(
      map(({order, history, countries}) => ({
        order,
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
