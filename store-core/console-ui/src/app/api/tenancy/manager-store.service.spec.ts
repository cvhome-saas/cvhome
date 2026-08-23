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

  /*
   * The pod goes in the **body**, wrapped — `ListManagerStoreQuery.pod` is a `PodId` record, so a bare string
   * binds to nothing and the filter silently returns every store on the platform. That is the failure worth a
   * spec: it looks like a working page showing the wrong rows.
   */
  it('filters the store list by pod, with the id wrapped and count paging', () => {
    service.listByPod('507f1f77bcf86cd799439011', 1, 20).subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${BASE}/list`);
    expect(request.request.method).toBe('POST');
    // `name` is present and null rather than omitted: the server reads the whole query object, and
    // an absent field and an explicitly-empty one should not be two shapes on the wire.
    expect(request.request.body).toEqual({pod: {id: '507f1f77bcf86cd799439011'}, name: null});
    expect(request.request.params.get('page')).toBe('1');
    expect(request.request.params.get('count')).toBe('20');
    expect(request.request.params.has('size')).toBeFalse();
    request.flush({content: [], totalElements: 0, totalPages: 0, size: 20, number: 0});
  });

  /*
   * The term goes in `name`, which the server matches as a case-insensitive substring of the store's
   * name **or** its id. It used to be an equality on the name alone — a lookup, not a search.
   */
  it('sends a store search term as the name filter, trimmed', () => {
    service.listByPod('507f1f77bcf86cd799439011', 0, 20, '  store1 ').subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${BASE}/list`);
    expect(request.request.body).toEqual({pod: {id: '507f1f77bcf86cd799439011'}, name: 'store1'});
    request.flush({content: [], totalElements: 0, totalPages: 0, size: 20, number: 0});
  });

  it('reads every pod’s store count in one request', () => {
    service.storesPerPod().subscribe();
    const request = http.expectOne(scoped(`${BASE}/stores-per-pod`));
    expect(request.request.method).toBe('GET');
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
