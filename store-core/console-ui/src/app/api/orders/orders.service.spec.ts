import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {OrdersService} from './orders.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape.
 * None of it is checked by the compiler — a wrong path is still a string — and this tier had one
 * spec for twenty-four files. A contract change is a red test here rather than a QA finding two
 * modules later.
 */
describe('OrdersService', () => {
  let service: OrdersService;
  let http: ReturnType<typeof apiHarness<OrdersService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(OrdersService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const BASE = '/spg/checkout/api/v1';

  it('reads the paged list with its filters and the store', () => {
    service.list({page: 1, count: 20, status: 'SHIPPED'}).subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${BASE}/private/orders`);

    expect(request.request.method).toBe('GET');
    // `count`, not `size`: `ServletWebConfig` names the page-size parameter platform-wide.
    expect(request.request.params.get('count')).toBe('20');
    expect(request.request.params.get('page')).toBe('1');
    expect(request.request.params.get('status')).toBe('SHIPPED');
    expect(request.request.params.get('store')).toBe(TEST_STORE);
    request.flush({content: [], size: 0, totalElements: 0, totalPages: 0, pageNumber: 1});
  });

  it('reads one order by id', () => {
    service.get(42).subscribe();
    const request = http.expectOne(`${BASE}/private/orders/42?store=${TEST_STORE}`);
    expect(request.request.method).toBe('GET');
    request.flush({} as never);
  });

  it('reads and appends the status history on the same path', () => {
    service.history(42).subscribe();
    http.expectOne(`${BASE}/private/orders/42/history?store=${TEST_STORE}`).flush([]);

    service.addHistory(42, {status: 'SHIPPED', comments: 'Handed over'} as never).subscribe();
    const post = http.expectOne(`${BASE}/private/orders/42/history?store=${TEST_STORE}`);

    expect(post.request.method).toBe('POST');
    expect(post.request.body).toEqual({status: 'SHIPPED', comments: 'Handed over'});
    post.flush(null);
  });

  it('reads the store’s countries', () => {
    service.countries().subscribe();
    http.expectOne(`${BASE}/country?store=${TEST_STORE}`).flush([]);
  });

  it('finds the one order behind a ref, and fails when the store has none', () => {
    let found: number | undefined;
    service.byRef('9f1c7e2a-0000-4000-8000-000000000001').subscribe((order) => (found = order.id));
    const list = http.expectOne((candidate) => candidate.url === `${BASE}/private/orders`);
    expect(list.request.params.get('ref')).toBe('9f1c7e2a-0000-4000-8000-000000000001');
    expect(list.request.params.get('count')).toBe('1');
    list.flush({content: [{id: 4187}], totalElements: 1});
    http.expectOne(`${BASE}/private/orders/4187?store=${TEST_STORE}`).flush({id: 4187, products: [{id: 1}]});
    expect(found).toBe(4187);

    let failed = false;
    service.byRef('missing').subscribe({error: () => (failed = true)});
    http.expectOne((candidate) => candidate.url === `${BASE}/private/orders`).flush({content: [], totalElements: 0});
    expect(failed).toBe(true);
  });
});
