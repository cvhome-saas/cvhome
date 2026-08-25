import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {ManufacturerService} from './manufacturer.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string — and this tier had one spec for
 * twenty-four files.
 */
describe('ManufacturerService', () => {
  let service: ManufacturerService;
  let http: ReturnType<typeof apiHarness<ManufacturerService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(ManufacturerService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const page = {content: [], size: 0, totalElements: 0, totalPages: 0, pageNumber: 0};
  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/spg/catalog/api/v1';

  /* Plural to list, singular to act on one: the pod's own naming, and easy to get backwards. */
  it('lists on the plural path and acts on the singular one', () => {
    service.list({page: 0, count: 500}).subscribe();
    http.expectOne((candidate) => candidate.url === `${BASE}/private/manufacturers`).flush(page);

    service.get(2).subscribe();
    http.expectOne(scoped(`${BASE}/private/manufacturer/2`)).flush({} as never);

    service.create({code: 'nike'} as never).subscribe();
    const created = http.expectOne(scoped(`${BASE}/private/manufacturer`));
    expect(created.request.method).toBe('POST');
    created.flush({} as never);

    service.update(2, {code: 'nike'} as never).subscribe();
    expect(http.expectOne(scoped(`${BASE}/private/manufacturer/2`)).request.method).toBe('PUT');

    service.delete(2).subscribe();
    expect(http.expectOne(scoped(`${BASE}/private/manufacturer/2`)).request.method).toBe('DELETE');
  });

  it('asks whether a code is taken', () => {
    service.codeTaken('nike').subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${BASE}/private/manufacturer/unique`);
    expect(request.request.params.get('code')).toBe('nike');
    request.flush({exists: true});
  });
});
