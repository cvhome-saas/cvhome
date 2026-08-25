import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {PaymentConfigurationService} from './payment-configuration.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string.
 */
describe('PaymentConfigurationService', () => {
  let service: PaymentConfigurationService;
  let http: ReturnType<typeof apiHarness<PaymentConfigurationService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(PaymentConfigurationService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/spg/payment/api/v1/private/payment-configuration';

  it('reads the supported types and the store’s configurations', () => {
    service.supportedTypes().subscribe();
    http.expectOne(scoped(`${BASE}/supported-payment-types`)).flush([]);

    service.configs().subscribe();
    http.expectOne(scoped(BASE)).flush([]);
  });

  /* Create posts to the collection; update puts to the payment type, not to an id. */
  it('creates on the collection and updates on the payment type', () => {
    service.create({paymentType: 'STRIPE'} as never).subscribe();
    const created = http.expectOne(scoped(BASE));
    expect(created.request.method).toBe('POST');
    created.flush(null);

    service.update('STRIPE', {paymentType: 'STRIPE'} as never).subscribe();
    const updated = http.expectOne(scoped(`${BASE}/STRIPE`));
    expect(updated.request.method).toBe('PUT');
    updated.flush(null);
  });
});
