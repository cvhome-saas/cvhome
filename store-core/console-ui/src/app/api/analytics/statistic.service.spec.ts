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
   * There is deliberately no third call here. `subscription-statistic` exists in no Java file on the
   * platform — see lessons.md, "Platform — no subscription statistics".
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
});
