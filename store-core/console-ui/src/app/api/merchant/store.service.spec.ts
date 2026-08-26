import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {MerchantStoreService} from './store.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string.
 */
describe('MerchantStoreService', () => {
  let service: MerchantStoreService;
  let http: ReturnType<typeof apiHarness<MerchantStoreService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(MerchantStoreService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/spg/merchant/api/v1';

  it('reads, updates and deletes the open store on one path', () => {
    service.store().subscribe();
    http.expectOne(scoped(`${BASE}/private/store`)).flush({} as never);

    service.update({code: 'ACME'} as never).subscribe();
    expect(http.expectOne(scoped(`${BASE}/private/store`)).request.method).toBe('PUT');

    service.delete().subscribe();
    expect(http.expectOne(scoped(`${BASE}/private/store`)).request.method).toBe('DELETE');
  });

  /*
   * Social links and slider images are `PUT`s of the *whole* store, not patches — which is why the
   * sections re-send everything they hold rather than only what changed.
   */
});
