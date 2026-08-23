/** Ported from seller-ui/projects/seller-core/analytics/src/lib/services/statistic.api.service.ts. */
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {StatisticList, StatisticRange} from '@models/statistics';

/**
 * Ported from seller-ui/projects/seller-core/analytics/src/lib/services/statistic.api.service.ts.
 *
 * The merchant statistics, all served by checkout through the pod gateway.
 *
 * Each returns the same `(date, name, value)` triple and each means something different by it. The
 * comments below are the queries, read from `OrderRepository` and `OrderProductRepository` — without
 * them the shape says nothing, and the console has already shipped one chart mislabelled because of
 * that (see `customerStatistic`).
 *
 * seller-core also has `store-statistic`, `org-statistic` and `subscription-statistic` on tenancy.
 * The first two feed the *platform admin* dashboard and Module 11 ported them — they are at the foot
 * of this class, on tenancy's base rather than checkout's. The third was never ported because
 * **it does not exist**: `subscription-statistic` appears in no Java file on the platform, and
 * seller-ui's admin home has been calling a 404 since it was written. See lessons.md, "Platform — no
 * subscription statistics".
 */
const CHECKOUT_STATISTIC_BASE = '/spg/checkout/api/v2/private';

/**
 * Tenancy's own counters. A different service and a different audience: these are platform figures,
 * not one merchant's, and both are `hasRole('ROLE_SUPER_ADMIN')`.
 */
const TENANCY_STATISTIC_BASE = '/tenancy/api/v2/private';

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
}
