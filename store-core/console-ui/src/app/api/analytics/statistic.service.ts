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
 * They feed the *platform admin* dashboard, not this one, and are not ported.
 */
const CHECKOUT_STATISTIC_BASE = '/spg/checkout/api/v2/private';

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
}
