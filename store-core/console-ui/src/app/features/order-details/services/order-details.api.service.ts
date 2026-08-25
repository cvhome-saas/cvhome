import {Injectable, inject} from '@angular/core';
import {Observable, catchError, forkJoin, map, of} from 'rxjs';

import {OrdersService} from '@api/orders/orders.service';
import {PaymentService} from '@api/payment/payment.service';
import {ManagerStoreService} from '@api/tenancy/manager-store.service';
import type {MerchantStore} from '@models/merchant';
import type {PaymentTransaction} from '@models/payment';
import type {
  OrderStatus,
  PersistableOrderStatusHistory,
  ReadableOrder,
  ReadableOrderStatusHistory,
} from '@models/checkout';

/**
 * How many of an order's payments the panel reads.
 *
 * An order has one payment, or a handful if the first attempts failed. There is no endpoint that
 * counts them, so this is a cap rather than a page size — the panel does not paginate.
 */
const PAYMENTS_PER_ORDER = 20;

/** One order, its timeline, its payments, and the country names its address codes resolve to. */
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
  /**
   * The payments taken against this order.
   *
   * Empty both when the order has none and when the payment service could not be reached — the two
   * are not distinguishable from here, and a panel saying "no payments recorded" is a better answer
   * than a page that failed over a secondary read.
   */
  readonly payments: readonly PaymentTransaction[];
}

/**
 * The order detail screen's data.
 *
 * Five calls. Three are required — an order with no timeline is a half-rendered page, and the
 * country lookup is what turns `DE` into `Germany` in both address panels. The other two are
 * optional: the selling store backs the invoice's letterhead, and the payments back one panel.
 *
 * Only one write exists — `addHistory`. The design also asks for refund, capture, cancel, duplicate,
 * shipment creation and address editing; none of them is mapped in checkout. See lessons.md.
 */
@Injectable({providedIn: 'root'})
export class OrderDetailsApi {
  private readonly orders = inject(OrdersService);
  private readonly stores = inject(ManagerStoreService);
  private readonly payments = inject(PaymentService);

  load(orderId: number, storeId: string): Observable<OrderDetail> {
    return forkJoin({
      order: this.orders.get(orderId),
      history: this.orders.history(orderId),
      countries: this.orders.countries(),
      // Optional, unlike the other three: the letterhead is the only thing that reads it, and an
      // order is still an order when the merchant service cannot be reached.
      seller: this.stores.getStoreDetail(storeId).pipe(catchError(() => of(null))),
      /*
       * The order's payments, found by the one link there is: checkout writes the order id into the
       * payment request's `ref`, which lands in `Transaction.requestRef`. It is a convention held in
       * one line of a different service, not a typed relation — see lessons.md, "Payments — the link
       * from a transaction to its order is a convention". Optional, like the letterhead: a payments
       * outage costs this panel and nothing else.
       */
      payments: this.payments
        .transactions({requestRef: String(orderId), page: 0, count: PAYMENTS_PER_ORDER})
        .pipe(
          map((page) => page.content),
          catchError(() => of([] as readonly PaymentTransaction[])),
        ),
    }).pipe(
      map(({order, history, countries, seller, payments}) => ({
        order,
        seller,
        // Newest first: a re-tried payment is the one being asked about.
        payments: [...payments].sort(byTransactionDate),

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

function byTransactionDate(a: PaymentTransaction, b: PaymentTransaction): number {
  return new Date(b.transactionDate ?? 0).getTime() - new Date(a.transactionDate ?? 0).getTime();
}
