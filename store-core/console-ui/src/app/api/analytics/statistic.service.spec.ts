import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {StatisticService} from './statistic.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string — and this tier had one spec for
 * twenty-four files.
 */
describe('StatisticService', () => {
  let service: StatisticService;
  let http: ReturnType<typeof apiHarness<StatisticService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(StatisticService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/spg/checkout/api/v2/private';
  const RANGE = {startDate: '2026-01-01', endDate: '2026-01-31'} as never;

  /*
   * All three are POSTs carrying a range, not GETs with parameters — and all three counted, rather
   * than summed, which is why the dashboard has no revenue. See lessons.md, "Dashboard — no revenue
   * anywhere".
   */
  it('posts a range to each of the three merchant statistics', () => {
    service.orderStatistic(RANGE).subscribe();
    const orders = http.expectOne(scoped(`${BASE}/order-statistic`));
    expect(orders.request.method).toBe('POST');
    expect(orders.request.body).toEqual(RANGE);
    orders.flush({statistics: []} as never);

    service.customerStatistic(RANGE).subscribe();
    http.expectOne(scoped(`${BASE}/customer-statistic`)).flush({statistics: []} as never);

    service.productStatistic(RANGE).subscribe();
    http.expectOne(scoped(`${BASE}/product-statistic`)).flush({statistics: []} as never);
  });

  /*
   * The two platform counters live on **tenancy**, not checkout, and that is the whole point of
   * asserting them: the merchant three are pod-side and these are store-core-side, so a copy-paste
   * of the base path would have sent a super admin's request to the open store's pod.
   *
   * Billing's two aggregates live on a third base again, and are asserted separately below.
   */
  it('posts the same range to tenancy for the two platform statistics', () => {
    const TENANCY = '/tenancy/api/v2/private';

    service.orgStatistic(RANGE).subscribe();
    const orgs = http.expectOne(scoped(`${TENANCY}/org-statistic`));
    expect(orgs.request.method).toBe('POST');
    expect(orgs.request.body).toEqual(RANGE);
    orgs.flush({entries: []});

    service.storeStatistic(RANGE).subscribe();
    http.expectOne(scoped(`${TENANCY}/store-statistic`)).flush({entries: []});
  });

  /*
   * A third base, and the one this file previously had a paragraph explaining the absence of.
   * `subscription-statistic` existed in no Java file on the platform, so seller-ui's admin home had
   * been calling a 404 for its entire life — on this exact path, which is why the base is asserted
   * rather than assumed.
   */
  it('posts the same range to billing for the two money statistics', () => {
    const BILLING = '/billing/api/v2/private';

    service.revenueStatistic(RANGE).subscribe();
    const revenue = http.expectOne(scoped(`${BILLING}/revenue-statistic`));
    expect(revenue.request.method).toBe('POST');
    expect(revenue.request.body).toEqual(RANGE);
    revenue.flush({entries: []});

    service.subscriptionStatistic(RANGE).subscribe();
    const subscriptions = http.expectOne(scoped(`${BILLING}/subscription-statistic`));
    expect(subscriptions.request.method).toBe('POST');
    subscriptions.flush({entries: []});
  });

  /*
   * The five platform statistics are split across three services, and a copy-pasted base is the
   * failure that would be invisible: a super admin's request sent to the open store's pod comes back
   * 200 with somebody else's numbers rather than an error.
   */
  it('keeps the merchant, tenancy and billing bases distinct', () => {
    const bases = new Set<string>();
    const record = (url: string) => bases.add(url.slice(0, url.lastIndexOf('/')));

    service.orderStatistic(RANGE).subscribe();
    record(http.expectOne((it) => it.url.endsWith('/order-statistic')).request.url);
    service.orgStatistic(RANGE).subscribe();
    record(http.expectOne((it) => it.url.endsWith('/org-statistic')).request.url);
    service.revenueStatistic(RANGE).subscribe();
    record(http.expectOne((it) => it.url.endsWith('/revenue-statistic')).request.url);

    expect([...bases].sort()).toEqual([
      '/billing/api/v2/private',
      '/spg/checkout/api/v2/private',
      '/tenancy/api/v2/private',
    ]);
    http.match(() => true).forEach((request) => request.flush({entries: []}));
  });
});
