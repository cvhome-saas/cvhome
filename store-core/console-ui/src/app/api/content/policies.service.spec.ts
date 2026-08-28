import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {PoliciesService} from './policies.service';

describe('PoliciesService', () => {
  let service: PoliciesService;
  let http: ReturnType<typeof apiHarness<PoliciesService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(PoliciesService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/spg/content/api/v1/private/content/policies';

  it('reads compliance, scoped to the store', () => {
    service.compliance().subscribe();
    http.expectOne(scoped(`${BASE}/compliance`)).flush([]);
  });

  it('reads the version history and one version', () => {
    service.versions(3).subscribe();
    http.expectOne(scoped(`${BASE}/3/versions`)).flush([]);

    service.version(3, 2).subscribe();
    http.expectOne(scoped(`${BASE}/3/versions/2`)).flush({} as never);
  });

  it('publishes a version and restores an old version onto the head', () => {
    const body = {note: 'GDPR pass'} as never;
    service.publishVersion(3, body).subscribe();
    const published = http.expectOne(scoped(`${BASE}/3/publish-version`));
    expect(published.request.method).toBe('POST');
    expect(published.request.body).toEqual(body);
    published.flush({} as never);

    service.restoreText(3, 2).subscribe();
    const restored = http.expectOne(scoped(`${BASE}/3/versions/2/restore-text`));
    expect(restored.request.method).toBe('POST');
    expect(restored.request.body).toBeNull();
    restored.flush({} as never);
  });
});
