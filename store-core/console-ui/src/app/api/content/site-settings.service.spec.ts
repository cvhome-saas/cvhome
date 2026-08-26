import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {SiteSettingsService} from './site-settings.service';

describe('SiteSettingsService', () => {
  let service: SiteSettingsService;
  let http: ReturnType<typeof apiHarness<SiteSettingsService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(SiteSettingsService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const BASE = '/spg/content/api/v1/private/content/site-settings';
  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  it('reads the store record with no id in the path, because there is one per store', () => {
    service.get().subscribe();

    const request = http.expectOne(scoped(BASE));
    expect(request.request.method).toBe('GET');
    request.flush({logoMediaId: null, seo: {}, socialLinks: []});
  });

  /*
   * A null slot is the whole point: merchant, which used to own the logo, had upload endpoints and
   * no delete, so a logo could be set and never cleared.
   */
  it('replaces the whole record, so a null media slot clears it', () => {
    service
      .put({
        logoMediaId: null,
        logoDarkMediaId: null,
        faviconMediaId: 4,
        ogMediaId: null,
        seo: {metaTitle: {en: 'Acme'}},
        socialLinks: [{provider: 'INSTAGRAM', url: 'https://instagram.com/acme'}],
      })
      .subscribe();

    const request = http.expectOne(scoped(BASE));
    expect(request.request.method).toBe('PUT');
    expect(request.request.body.logoMediaId).toBeNull();
    expect(request.request.body.faviconMediaId).toBe(4);
    request.flush({});
  });
});
