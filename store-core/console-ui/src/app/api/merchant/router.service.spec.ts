import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {MerchantRouterService} from './router.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string — and this tier had one spec for
 * twenty-four files.
 */
describe('MerchantRouterService', () => {
  let service: MerchantRouterService;
  let http: ReturnType<typeof apiHarness<MerchantRouterService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(MerchantRouterService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/spg/merchant/api/v1/router';

  it('lists the store’s allocated domains', () => {
    service.allocations().subscribe();
    http.expectOne(scoped(`${BASE}/private/allocates`)).flush([]);
  });

  /* The domain travels as a parameter, not in a body — and the paths are not symmetrical. */
  it('allocates and removes a domain by parameter', () => {
    service.allocate('shop.example.com').subscribe();
    const allocated = http.expectOne((candidate) => candidate.url === `${BASE}/private/allocate`);
    expect(allocated.request.method).toBe('POST');
    expect(allocated.request.params.get('domain')).toBe('shop.example.com');
    allocated.flush(null);

    service.remove('shop.example.com').subscribe();
    const removed = http.expectOne((candidate) => candidate.url === `${BASE}/private/remove`);
    expect(removed.request.method).toBe('DELETE');
    expect(removed.request.params.get('domain')).toBe('shop.example.com');
    removed.flush(null);
  });
});
