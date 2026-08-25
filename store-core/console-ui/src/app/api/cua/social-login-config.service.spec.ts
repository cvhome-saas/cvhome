import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {SocialLoginConfigService} from './social-login-config.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string.
 */
describe('SocialLoginConfigService', () => {
  let service: SocialLoginConfigService;
  let http: ReturnType<typeof apiHarness<SocialLoginConfigService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(SocialLoginConfigService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/spg/cua/api/v1/private/social-login-config';

  it('reads the supported providers and the store’s configurations', () => {
    service.supportedProviders().subscribe();
    http.expectOne(scoped(`${BASE}/supported-social-providers`)).flush([]);

    service.configs().subscribe();
    http.expectOne(scoped(BASE)).flush([]);
  });

  /*
   * A **list**, not one object. seller-core's signature obscured that the endpoint takes
   * `List<PersistableSocialLoginConfig>`, which is why every provider goes out on every save.
   */
  it('saves every provider in one post, as an array', () => {
    service.save([{providerId: 'GOOGLE'}, {providerId: 'GITHUB'}] as never).subscribe();
    const request = http.expectOne(scoped(BASE));

    expect(request.request.method).toBe('POST');
    expect(Array.isArray(request.request.body)).toBeTrue();
    expect((request.request.body as unknown[]).length).toBe(2);
    request.flush(null);
  });
});
