import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {SnippetsService} from './snippets.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string.
 */
describe('SnippetsService', () => {
  let service: SnippetsService;
  let http: ReturnType<typeof apiHarness<SnippetsService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(SnippetsService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/spg/content/api/v1/private/content/snippets';

  it('lists, reads a snippet by code and upserts it with PUT', () => {
    service.list().subscribe();
    http.expectOne(scoped(BASE)).flush([]);

    service.get('LANDING_PAGE').subscribe();
    http.expectOne(scoped(`${BASE}/LANDING_PAGE`)).flush({} as never);

    service.put('LANDING_PAGE', {visible: true, translations: []}).subscribe();
    const updated = http.expectOne(scoped(`${BASE}/LANDING_PAGE`));
    expect(updated.request.method).toBe('PUT');
    updated.flush({id: 4, code: 'LANDING_PAGE', visible: true, translations: []});
  });
});
