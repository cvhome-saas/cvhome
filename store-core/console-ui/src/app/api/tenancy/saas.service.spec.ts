import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {SaasService} from './saas.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string.
 */
describe('SaasService', () => {
  let service: SaasService;
  let http: ReturnType<typeof apiHarness<SaasService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(SaasService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/tenancy/api/v1';

  it('reads the platform’s public properties and the store’s pod', () => {
    service.saasProperties().subscribe();
    http.expectOne(scoped(`${BASE}/saas/public/saas-properties`)).flush({} as never);

    service.storePod().subscribe();
    http.expectOne(scoped(`${BASE}/router/store-pod-by-store-id`)).flush({} as never);
  });
});
