import {apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {PaymentService} from './payment.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string.
 */
describe('PaymentService', () => {
  let service: PaymentService;
  let http: ReturnType<typeof apiHarness<PaymentService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(PaymentService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const page = {content: [], size: 0, totalElements: 0, totalPages: 0, pageNumber: 0};

  const BASE = '/spg/payment/api/v1/private/payment';

  it('reads a page of transactions with its filters', () => {
    service.transactions({status: 'WAITING_VERIFICATION', page: 0, count: 20}).subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${BASE}/transactions`);
    expect(request.request.params.get('status')).toBe('WAITING_VERIFICATION');
    expect(request.request.params.get('count')).toBe('20');
    request.flush(page);
  });

  /*
   * There is no count endpoint, so the only way to learn how many of something there are is to
   * fetch one and read the envelope. See lessons.md, "Dashboard — counting requires fetching".
   */
  it('counts by asking for a single row and reading the total off the page', () => {
    let counted: number | undefined;
    service.countByStatus('WAITING_VERIFICATION').subscribe((value) => (counted = value));

    const request = http.expectOne((candidate) => candidate.url === `${BASE}/transactions`);
    expect(request.request.params.get('count')).toBe('1');
    request.flush({...page, totalElements: 17});

    expect(counted).toBe(17);
  });
});
