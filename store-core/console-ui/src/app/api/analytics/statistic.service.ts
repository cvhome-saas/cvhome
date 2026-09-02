import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@cvhome-saas/ui-kit';
import type {StatisticList, StatisticRange} from '@models/statistics';

/**
 * The merchant statistics, all served by checkout through the pod gateway.
 *
 * Each returns the same `(date, name, value)` triple and each means something different by it. The
 * comments below are the queries, read from `OrderRepository` and `OrderProductRepository` — without
 * them the shape says nothing, and the console has already shipped one chart mislabelled because of
 * that (see `customerStatistic`).
 *
 * seller-core also has `store-statistic`, `org-statistic` and `subscription-statistic`. The first
 * two feed the *platform admin* dashboard and Module 11 ported them — they are at the foot of this
 * class, on tenancy's base rather than checkout's.
 *
 * **The third could not be ported, because for its entire life it did not exist.**
 * `subscription-statistic` appeared in no Java file on the platform: seller-ui's admin home called
 * `billing/api/v2/private/subscription-statistic` from the day it was written and got a 404 every
 * time, rendering as an empty plot rather than as an error. It exists now, on
 * {@link BILLING_STATISTIC_BASE}, along with the revenue figure the platform also had nowhere to
 * ask for. The path seller-ui guessed at turned out to be the right one; nothing had ever served it.
 */
const CHECKOUT_STATISTIC_BASE = '/spg/checkout/api/v2/private';

/**
 * Tenancy's own counters. A different service and a different audience: these are platform figures,
 * not one merchant's, and both are `hasRole('ROLE_SUPER_ADMIN')`.
 */
const TENANCY_STATISTIC_BASE = '/tenancy/api/v2/private';

/**
 * Billing's own aggregates, and the first money figures on this platform.
 *
 * Super-admin only, like tenancy's. A separate base because they are a different service behind the
 * same gateway, not because they are a different kind of question — all four are the operator's.
 */
const BILLING_STATISTIC_BASE = '/billing/api/v2/private';

@Injectable({providedIn: 'root'})
export class StatisticService {
  private readonly crudService = inject(CrudService);

  /**
   * Orders per day per status:
   * `select (day(datePurchased), status, count(id)) … group by day(datePurchased), status`.
   *
   * The status is the raw `OrderStatus` name — one of ten — so callers must derive their series from
   * the response rather than assuming a fixed set.
   */
  orderStatistic(range: StatisticRange): Observable<StatisticList> {
    return this.crudService.post(`${CHECKOUT_STATISTIC_BASE}/order-statistic`, range);
  }

  /**
   * **Orders grouped by billing country** — `select (null, billing.country, count(id)) … group by
   * billing.country`. Despite the name it counts orders, not customers: a store with one loyal German
   * buyer reads the same as one with forty. See lessons.md, "Dashboard — customer-statistic counts
   * orders, not customers".
   */
  customerStatistic(range: StatisticRange): Observable<StatisticList> {
    return this.crudService.post(`${CHECKOUT_STATISTIC_BASE}/customer-statistic`, range);
  }

  /**
   * Order lines per SKU — `select (null, op.sku, count(o.id)) from OrderProduct … group by op.sku`.
   *
   * The SKU, not the product's name, and a count of *orders containing it* rather than units sold: a
   * ten-unit order counts once. See lessons.md, "Dashboard — product-statistic has no name and no
   * quantity".
   */
  productStatistic(range: StatisticRange): Observable<StatisticList> {
    return this.crudService.post(`${CHECKOUT_STATISTIC_BASE}/product-statistic`, range);
  }

  /**
   * **Organizations created per day, across the whole platform** —
   * `ManagerOrgRepository.orgStatistic`, grouped by day of `created_date`.
   *
   * **`name` is null on every entry.** The query selects `date` and `value` only — there is nothing
   * to group organizations by — so callers must read the pair and never the triple. Super-admin
   * only, and not scopeable to one org: it is a business metric for the operator, not tenant data.
   */
  orgStatistic(range: StatisticRange): Observable<StatisticList> {
    return this.crudService.post(`${TENANCY_STATISTIC_BASE}/org-statistic`, range);
  }

  /**
   * **Stores created per day, across every organization** — `ManagerStoreRepository.storeStatistic`.
   *
   * Platform-wide, so it is not the open store's anything; the merchant dashboard has no equivalent
   * and does not want one. `name` is null here too, for the same reason.
   */
  storeStatistic(range: StatisticRange): Observable<StatisticList> {
    return this.crudService.post(`${TENANCY_STATISTIC_BASE}/store-statistic`, range);
  }

  /**
   * **Money actually collected per day, per currency** — `SubscriptionInvoiceRepository
   * .revenueStatistic`, `sum(amount_paid)` over settled invoices only.
   *
   * **`name` is the ISO currency code**, which makes these the first entries on the platform whose
   * `name` is not null: the two tenancy counters group by nothing. Callers must read the triple and
   * **must not sum across currencies** — nothing on the platform holds an exchange rate, so a merged
   * total would be a wrong number rather than a missing one.
   *
   * **`value` is in minor units**, as everything in billing is. Divide by a hundred at the point of
   * display, never here.
   *
   * Keyed on `paid_at` rather than `issued_at`, so a past day's figure changes only when money
   * actually moved on that day — a late payment does not silently rewrite last month's chart.
   */
  revenueStatistic(range: StatisticRange): Observable<StatisticList> {
    return this.crudService.post(`${BILLING_STATISTIC_BASE}/revenue-statistic`, range);
  }

  /**
   * **Subscriptions started per day, by plan code** — read from `subscription_audit`, not from
   * `store_subscription.created_date`.
   *
   * The distinction is the whole meaning of the number: a `store_subscription` row is written by
   * provisioning the moment a store is created, so `created_date` counts every store that ever
   * entered billing including the ones that never paid — which is already what {@link
   * storeStatistic} answers. "Started" here means started paying or trialling.
   *
   * `name` is the plan code, so the console can stack the series. A store whose plan id no longer
   * resolves comes back as `UNKNOWN` rather than being dropped.
   */
  subscriptionStatistic(range: StatisticRange): Observable<StatisticList> {
    return this.crudService.post(`${BILLING_STATISTIC_BASE}/subscription-statistic`, range);
  }
}
