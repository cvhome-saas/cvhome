import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {ManagerStoreService} from './manager-store.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string — and this tier had one spec for
 * twenty-four files.
 */
describe('ManagerStoreService', () => {
  let service: ManagerStoreService;
  let http: ReturnType<typeof apiHarness<ManagerStoreService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(ManagerStoreService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/tenancy/api/v1/store-manager';

  it('lists the account’s stores', () => {
    service.list().subscribe();
    const request = http.expectOne((candidate) => candidate.url.startsWith(`${BASE}/list`));
    expect(request.request.method).toBe('POST');
    request.flush([]);
  });

  it('re-reads one store’s row while it provisions', () => {
    service.storeInfo('ORG1-STORE2').subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${BASE}/store-info`);
    expect(request.request.params.get('store')).toBe('ORG1-STORE2');
    request.flush({} as never);
  });

  it('reads a store’s detail by code, and creates one by posting the request', () => {
    service.getStoreDetail('ORG1-STORE1').subscribe();
    http.expectOne(scoped(`${BASE}/private/store/ORG1-STORE1`)).flush({} as never);

    service.create({name: 'Acme'} as never).subscribe();
    const created = http.expectOne(scoped(`${BASE}/private/store`));
    expect(created.request.method).toBe('POST');
    expect(created.request.body).toEqual({name: 'Acme'});
    created.flush({} as never);
  });

  it('reads the public theme lists, which need no store', () => {
    service.themes().subscribe();
    http.expectOne(scoped(`${BASE}/public/themes`)).flush([]);

    service.colorThemes().subscribe();
    http.expectOne(scoped(`${BASE}/public/color-themes`)).flush([]);
  });
});
