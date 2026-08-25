import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {BannersService} from './banners.service';

describe('BannersService', () => {
  let service: BannersService;
  let http: ReturnType<typeof apiHarness<BannersService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(BannersService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const BASE = '/spg/content/api/v1/private/content/banners';

  it('asks what a placement shows right now, and everything when none is named', () => {
    service.effective('HERO').subscribe();
    const filtered = http.expectOne((r) => r.url === `${BASE}/effective`);
    expect(filtered.request.params.get('placement')).toBe('HERO');
    expect(filtered.request.params.get('store')).toBe(TEST_STORE);
    filtered.flush([]);

    service.effective().subscribe();
    const all = http.expectOne((r) => r.url === `${BASE}/effective`);
    // An absent filter must be absent, not the string "undefined".
    expect(all.request.params.has('placement')).toBeFalse();
    all.flush([]);
  });
});
