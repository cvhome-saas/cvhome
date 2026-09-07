import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {ImpersonationService} from './impersonation.service';

/*
 * The api tier's contract with the gateway: the path is on the gateway's own origin, the verb is the
 * one the gateway routes, and the body is the four fields the gateway validates by hand.
 */
describe('ImpersonationService', () => {
  let service: ImpersonationService;
  let http: ReturnType<typeof apiHarness<ImpersonationService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(ImpersonationService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  it('starts with the four fields the gateway validates, and ends with a delete', () => {
    const request = {userId: 'u', reason: 'ticket 42'};

    service.start(request).subscribe();
    const started = http.expectOne(scoped('/api/v1/impersonation'));
    expect(started.request.method).toBe('POST');
    expect(started.request.body).toEqual(request);
    started.flush({} as never);

    service.end().subscribe();
    const ended = http.expectOne(scoped('/api/v1/impersonation'));
    expect(ended.request.method).toBe('DELETE');
    ended.flush(null);
  });
});
