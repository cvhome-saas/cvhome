import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {PodService} from './pod.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string.
 */
describe('PodService', () => {
  let service: PodService;
  let http: ReturnType<typeof apiHarness<PodService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(PodService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  /*
   * Scoped to the caller: a super admin sees every pod, an org admin only its own private ones, and
   * an ordinary merchant an empty list — `listPlaceablePublicPods()` exists on the server and is
   * exposed on no endpoint. See lessons.md, "Shell — no merchant-readable list of placeable pods".
   */
  it('lists the pods this caller may place a store in', () => {
    service.list().subscribe();
    const request = http.expectOne(scoped('/pod-registry/api/v1/pod/list'));
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });
});
