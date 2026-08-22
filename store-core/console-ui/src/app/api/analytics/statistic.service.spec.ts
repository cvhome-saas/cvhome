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
});
