import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {ContentSummaryService} from './content-summary.service';

describe('ContentSummaryService', () => {
  let service: ContentSummaryService;
  let http: ReturnType<typeof apiHarness<ContentSummaryService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(ContentSummaryService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/spg/content/api/v1/private/content';

  it('reads the hub summary and the redirect ledger', () => {
    service.summary().subscribe();
    http.expectOne(scoped(`${BASE}/summary`)).flush({} as never);

    service.redirects().subscribe();
    http.expectOne(scoped(`${BASE}/redirects`)).flush([]);
  });
});
