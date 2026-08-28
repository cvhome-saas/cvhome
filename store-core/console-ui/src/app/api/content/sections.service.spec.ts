import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {SectionsService} from './sections.service';

describe('SectionsService', () => {
  let service: SectionsService;
  let http: ReturnType<typeof apiHarness<SectionsService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(SectionsService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  /*
   * The whole order in one PATCH. One move at a time leaves gaps and ties, which the storefront
   * then resolves however the database happens to answer.
   */
  it('sends the whole order in one request', () => {
    service.reorder([3, 1, 2]).subscribe();

    const request = http.expectOne(
      `/spg/content/api/v1/private/content/sections/reorder?store=${TEST_STORE}`,
    );
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual([3, 1, 2]);
    request.flush(null);
  });
});
