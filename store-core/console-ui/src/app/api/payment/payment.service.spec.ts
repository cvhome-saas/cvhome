import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
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

  /* A cleared filter is omitted, not sent as an empty string — the seller-ui behaviour this port fixed. */
  it('leaves an absent filter off the wire entirely', () => {
    service.transactions({page: 0, count: 20, status: undefined, requestRef: undefined}).subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${BASE}/transactions`);
    expect(request.request.params.has('status')).toBe(false);
    expect(request.request.params.has('requestRef')).toBe(false);
    request.flush(page);
  });

  /*
   * There is no count endpoint, so the only way to learn how many of something there are is to
   * fetch one and read the envelope. See lessons.md, "Dashboard — counting requires fetching".
   */
  it('counts by asking for a single row and reading the total off the page', () => {
    let counted: number | undefined;
    service.countByStatus('PAID').subscribe((value) => (counted = value));

    const request = http.expectOne((candidate) => candidate.url === `${BASE}/transactions`);
    expect(request.request.params.get('count')).toBe('1');
    request.flush({...page, totalElements: 17});

    expect(counted).toBe(17);
  });

  /*
   * The queue is PENDING + MANUAL_TRANSFER, never WAITING_VERIFICATION — that status is never set by
   * any processor, so counting it counts zero forever. This test is the guard on that.
   */
  it('counts the approval queue by status and gateway together, not by WAITING_VERIFICATION', () => {
    let counted: number | undefined;
    service.countAwaitingApproval().subscribe((value) => (counted = value));

    const request = http.expectOne((candidate) => candidate.url === `${BASE}/transactions`);
    expect(request.request.params.get('status')).toBe('PENDING');
    expect(request.request.params.get('paymentType')).toBe('MANUAL_TRANSFER');
    request.flush({...page, totalElements: 3});

    expect(counted).toBe(3);
  });

  it('carries a date range into the queue count', () => {
    service.countAwaitingApproval({transactionDateFrom: '2026-08-01T00:00:00.000Z'}).subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${BASE}/transactions`);
    expect(request.request.params.get('transactionDateFrom')).toBe('2026-08-01T00:00:00.000Z');
    request.flush(page);
  });

  /* Addressed by the UUID, not by `ReadableTransaction.id` — the mistake the numeric key invites. */
  it('approves against the internal ref, with the external transaction number as the body', () => {
    service.approve('9f42-kq81', {transactionNo: 'ACME-2291'}).subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${BASE}/transaction/9f42-kq81/approve`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({transactionNo: 'ACME-2291'});
    expect(request.request.params.get('store')).toBe(TEST_STORE);
    request.flush(null);
  });

  it('rejects against the internal ref, with no body of its own', () => {
    service.reject('9f42-kq81').subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${BASE}/transaction/9f42-kq81/reject`);
    expect(request.request.method).toBe('POST');
    expect(request.request.params.get('store')).toBe(TEST_STORE);
    request.flush(null);
  });
});
