import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {SignUpService} from './sign-up.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string.
 */
describe('SignUpService', () => {
  let service: SignUpService;
  let http: ReturnType<typeof apiHarness<SignUpService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(SignUpService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  /*
   * The one endpoint on the platform anyone may call, and it validates nothing — see lessons.md,
   * "Auth — public signup validates nothing". The console's own form is the only validation there
   * is, which is why the wire shape here has to stay exactly what tenancy expects.
   */
  it('posts the nested user the signup request expects', () => {
    service.signUp({user: {emailAddress: 'ada@example.com'}} as never).subscribe();
    const request = http.expectOne(scoped('/tenancy/api/v1/signup/public/create'));

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({user: {emailAddress: 'ada@example.com'}});
    request.flush({} as never);
  });
});
