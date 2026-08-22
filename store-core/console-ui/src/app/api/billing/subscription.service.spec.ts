import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {SubscriptionService} from './subscription.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape.
 * None of it is checked by the compiler — a wrong path is still a string — and this tier had one
 * spec for twenty-four files. A contract change is a red test here rather than a QA finding two
 * modules later.
 */
describe('SubscriptionService', () => {
  let service: SubscriptionService;
  let http: ReturnType<typeof apiHarness<SubscriptionService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(SubscriptionService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const BASE = '/billing/api/v1/subscription';

  it('reads the plan catalogue from a public path, optionally in one currency', () => {
    service.plans().subscribe();
    http.expectOne((candidate) => candidate.url === '/billing/api/v1/plan/public/plans').flush([]);

    service.plans('SAR').subscribe();
    const scoped = http.expectOne((candidate) => candidate.url === '/billing/api/v1/plan/public/plans');
    expect(scoped.request.params.get('currency')).toBe('SAR');
    scoped.flush([]);
  });

  it('reads the open store’s subscription', () => {
    service.current(TEST_STORE).subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${BASE}/current`);
    expect(request.request.params.get('store')).toBe(TEST_STORE);
    request.flush({} as never);
  });

  it('opens a checkout and changes a plan with the price, not the plan code', () => {
    service.checkout(TEST_STORE, 'price_1').subscribe();
    const checkout = http.expectOne((candidate) => candidate.url === `${BASE}/checkout`);
    expect(checkout.request.method).toBe('POST');
    expect(checkout.request.body).toEqual({planPriceId: 'price_1'});
    checkout.flush({url: 'https://pay.example.com'});

    service.changePlan(TEST_STORE, 'price_2').subscribe();
    const changed = http.expectOne((candidate) => candidate.url === `${BASE}/plan`);
    expect(changed.request.body).toEqual({planPriceId: 'price_2'});
    changed.flush({} as never);
  });

  /* Immediate cancellation is a super-admin operation and is never sent from the console. */
  it('cancels at the end of the period unless told otherwise', () => {
    service.cancel(TEST_STORE).subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${BASE}/cancel`);
    expect(request.request.body).toEqual({immediate: false});
    request.flush({} as never);
  });

  it('resumes a subscription that was set to lapse', () => {
    service.resume(TEST_STORE).subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${BASE}/resume`);
    expect(request.request.method).toBe('POST');
    request.flush({} as never);
  });
});
