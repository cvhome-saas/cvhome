import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {PodService} from './pod.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string.
 */
describe('PodService', () => {
  let service: PodService;
  let http: ReturnType<typeof apiHarness<PodService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(PodService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  /*
   * Scoped to the caller: a super admin sees every pod, an org admin only its own private ones, and
   * an ordinary merchant an empty list — `listPlaceablePublicPods()` exists on the server and is
   * exposed on no endpoint. See lessons.md, "Shell — no merchant-readable list of placeable pods".
   */
  it('lists the pods this caller may place a store in', () => {
    service.list().subscribe();
    const request = http.expectOne(scoped('/pod-registry/api/v1/pod/list'));
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  const BASE = '/pod-registry/api/v1/pod';
  const POD = '507f1f77bcf86cd799439011';

  /* `count`, not Spring's `size` — pod-registry depends on store-commons:autoconfigure like the rest. */
  it('pages the fleet with count, not size', () => {
    service.page(1, 20).subscribe();
    const request = http.expectOne((it) => it.url === BASE);
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('page')).toBe('1');
    expect(request.request.params.get('count')).toBe('20');
    expect(request.request.params.has('size')).toBeFalse();
    request.flush({content: [], totalElements: 0, totalPages: 0, size: 20, number: 0});
  });

  /* `q` matches the pod's name and endpoint server-side, and is omitted rather than sent empty. */
  it('sends a search term as q, trimmed, and omits an empty one', () => {
    service.page(0, 20, '  eu- ').subscribe();
    const searched = http.expectOne((it) => it.url === BASE && it.params.get('q') === 'eu-');
    searched.flush({content: [], totalElements: 0, totalPages: 0, size: 20, number: 0});

    service.page(0, 20, '   ').subscribe();
    const blank = http.expectOne((it) => it.url === BASE);
    expect(blank.request.params.has('q')).toBeFalse();
    blank.flush({content: [], totalElements: 0, totalPages: 0, size: 20, number: 0});
  });

  it('reads the registry’s own view by id', () => {
    service.find(POD).subscribe();
    const request = http.expectOne(scoped(`${BASE}/${POD}`));
    expect(request.request.method).toBe('GET');
    request.flush({});
  });

  /*
   * The owner is wrapped — `ManagerOrgId` is a record, so `orgId` is `{id}` and not a bare string —
   * and it is `null` for a public pod, which is what makes the server derive PUBLIC visibility.
   */
  it('wraps the owning org, and sends null for a public pod', () => {
    const endpoint = {endpoint: 'https://eu.pod', type: 'EXTERNAL'} as const;

    service.create({name: 'eu-central', endpoint, orgId: '65f023632bc46470c104b76f'}).subscribe();
    const priv = http.expectOne(scoped(BASE));
    expect(priv.request.method).toBe('POST');
    expect(priv.request.body).toEqual({name: 'eu-central', endpoint, orgId: {id: '65f023632bc46470c104b76f'}});
    priv.flush({});

    service.create({name: 'shared-1', endpoint, orgId: null}).subscribe();
    const shared = http.expectOne(scoped(BASE));
    expect(shared.request.body).toEqual({name: 'shared-1', endpoint, orgId: null});
    shared.flush({});
  });

  /*
   * Only name and endpoint. `PodServiceImpl.update` reads those two off the body and ignores the
   * rest, so sending visibility or capacity would be sending values the server drops — see
   * lessons.md, "Pods — visibility, region, capacity and owner cannot be edited".
   */
  it('updates only the two fields the server reads', () => {
    const endpoint = {endpoint: 'https://eu2.pod', type: 'INTERNAL'} as const;
    service.update(POD, {name: 'eu-central', endpoint}).subscribe();
    const request = http.expectOne(scoped(`${BASE}/${POD}`));
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({name: 'eu-central', endpoint});
    request.flush({});
  });

  it('drains and resumes by id, with no body', () => {
    service.drain(POD).subscribe();
    const drain = http.expectOne(scoped(`${BASE}/${POD}/drain`));
    expect(drain.request.method).toBe('POST');
    expect(drain.request.body).toBeNull();
    drain.flush({});

    service.resume(POD).subscribe();
    http.expectOne(scoped(`${BASE}/${POD}/resume`)).flush({});
  });

  it('deletes by id', () => {
    service.delete(POD).subscribe();
    const request = http.expectOne(scoped(`${BASE}/${POD}`));
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});
