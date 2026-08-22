import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {ContentBoxService} from './content-box.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string — and this tier had one spec for
 * twenty-four files.
 */
describe('ContentBoxService', () => {
  let service: ContentBoxService;
  let http: ReturnType<typeof apiHarness<ContentBoxService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(ContentBoxService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/spg/content/api/v1';

  /*
   * Content *boxes*, not the `private/content/any/{code}` paths seller-core calls — those are
   * mapped by no controller, so the old console's landing-page screen has never saved. See
   * lessons.md, "Store management — the landing-page endpoints seller-ui calls do not exist".
   */
  it('reads a box by code, creates one, and updates it by numeric id', () => {
    service.box('LANDING').subscribe();
    http.expectOne(scoped(`${BASE}/private/content/boxes/LANDING`)).flush({} as never);

    service.create({code: 'LANDING'} as never).subscribe();
    const created = http.expectOne(scoped(`${BASE}/private/content/box`));
    expect(created.request.method).toBe('POST');
    created.flush({id: 4});

    service.update(4, {code: 'LANDING'} as never).subscribe();
    const updated = http.expectOne(scoped(`${BASE}/private/content/box/4`));
    expect(updated.request.method).toBe('PUT');
    updated.flush(null);
  });
});
