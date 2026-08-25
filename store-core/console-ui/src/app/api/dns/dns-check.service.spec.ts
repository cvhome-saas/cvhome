import {apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {DnsCheckService, type CnameOutcome} from './dns-check.service';

const RESOLVER = 'https://dns.google/resolve';

describe('DnsCheckService', () => {
  let service: DnsCheckService;
  let http: ReturnType<typeof apiHarness<DnsCheckService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(DnsCheckService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  function resolve(body: object, target = 'pod-1.cvhome.app'): CnameOutcome | Error {
    let outcome: CnameOutcome | Error = new Error('nothing emitted');
    service.checkCname('shop.example.com', target).subscribe({
      next: (value) => (outcome = value),
      error: (failure: Error) => (outcome = failure),
    });
    http.expectOne((candidate) => candidate.url === RESOLVER).flush(body);
    return outcome;
  }

  /*
   * The tenant's ids must not reach Google. This is the one service that deliberately does *not*
   * go through `CrudService`, because that stamps `?store=` and `?pod=` onto every call and would
   * leak them to a third party — and would put the console's base URL in front of a foreign host.
   */
  it('asks a third-party resolver directly, carrying no tenant context', () => {
    service.checkCname('shop.example.com', 'pod-1.cvhome.app').subscribe();
    const request = http.expectOne((candidate) => candidate.url === RESOLVER);

    expect(request.request.params.get('name')).toBe('shop.example.com');
    expect(request.request.params.get('type')).toBe('CNAME');
    expect(request.request.params.get('store')).toBeNull();
    expect(request.request.params.get('pod')).toBeNull();
    request.flush({Status: 0});
  });

  it('busts the resolver’s cache, so a re-check after a fix sees the new record', () => {
    service.checkCname('shop.example.com', 'pod-1.cvhome.app').subscribe();
    const request = http.expectOne((candidate) => candidate.url === RESOLVER);
    expect(request.request.params.get('_cb')).toBeTruthy();
    request.flush({Status: 0});
  });

  it('recognises a record pointing here', () => {
    expect(
      resolve({Status: 0, Answer: [{name: 'shop.example.com.', type: 5, TTL: 300, data: 'pod-1.cvhome.app.'}]}),
    ).toBe('points-here');
  });

  it('distinguishes a record pointing somewhere else from no record at all', () => {
    expect(
      resolve({Status: 0, Answer: [{name: 'shop.example.com.', type: 5, TTL: 300, data: 'other.example.'}]}),
    ).toBe('points-elsewhere');

    expect(resolve({Status: 0, Answer: []})).toBe('no-record');
  });

  it('distinguishes a domain that does not exist', () => {
    expect(resolve({Status: 3})).toBe('no-such-domain');
  });

  /*
   * "Your DNS is wrong" and "we could not check" are different answers, and collapsing them told
   * operators to fix records that were fine. A lookup that fails throws rather than resolving to a
   * negative — the domain section renders that as "could not check" and does not block.
   */
  it('throws when the lookup itself fails, rather than reporting a negative', () => {
    let failed = false;
    service.checkCname('shop.example.com', 'pod-1.cvhome.app').subscribe({
      error: () => (failed = true),
    });
    http.expectOne((candidate) => candidate.url === RESOLVER)
      .flush(null, {status: 0, statusText: 'Unknown Error'});

    expect(failed).toBeTrue();
  });
});
