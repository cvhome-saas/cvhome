import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {OrgService} from './org.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string, which is exactly how seller-ui
 * shipped a `PUT org-manager/update` that no controller maps.
 */
describe('OrgService', () => {
  let service: OrgService;
  let http: ReturnType<typeof apiHarness<OrgService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(OrgService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const BASE = '/tenancy/api/v1/org-manager';
  const ORG = '65f023632bc46470c104b76f';

  /** Every request carries the request context's `?store=`, whether or not the endpoint reads it. */
  const scoped = (path: string, query = '') => `${path}?store=${TEST_STORE}${query}`;

  const emptyPage = {content: [], totalElements: 0, totalPages: 0, size: 20, number: 0};

  /*
   * `count`, not Spring's `size`: `store-commons:autoconfigure`'s ServletWebConfig renames the
   * page-size parameter platform-wide, and a `size` here would be silently ignored and every page
   * would come back at the server's default.
   */
  it('pages the org list with count, not size', () => {
    service.list(2, 20).subscribe();
    const request = http.expectOne((it) => it.url === `${BASE}/find-all`);
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('page')).toBe('2');
    expect(request.request.params.get('count')).toBe('20');
    expect(request.request.params.has('size')).toBeFalse();
    request.flush(emptyPage);
  });

  /*
   * A POST with a query body, matching `store-manager/list`. The term is one parameter spanning the
   * name and the email — a box that searched only the name would miss the rows the console is
   * showing, since almost every organization is listed by its email.
   */
  it('posts a search term and a status, trimmed, with nulls for what is not set', () => {
    service.search({term: '  nordwerk ', status: 'SUSPENDED'}, 1, 20).subscribe();
    const request = http.expectOne((it) => it.url === `${BASE}/list`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({term: 'nordwerk', status: 'SUSPENDED'});
    expect(request.request.params.get('page')).toBe('1');
    expect(request.request.params.get('count')).toBe('20');
    request.flush(emptyPage);
  });

  it('sends null rather than an empty string for a cleared box', () => {
    service.search({term: '   ', status: ''}, 0, 20).subscribe();
    const request = http.expectOne((it) => it.url === `${BASE}/list`);
    // The server normalises a blank term to "no filter" too, but sending `''` would make a cleared
    // box and an untouched one two different requests for one behaviour.
    expect(request.request.body).toEqual({term: null, status: null});
    request.flush(emptyPage);
  });

  it('reads one org by id', () => {
    service.findOne(ORG).subscribe();
    const request = http.expectOne((it) => it.url === `${BASE}/find-one`);
    expect(request.request.params.get('id')).toBe(ORG);
    request.flush({});
  });

  /*
   * The user is wrapped: `CreateOrgRequest` is `(PersistableUser user)`, so a flat body binds every
   * field to null and creates an organization owned by nobody.
   */
  it('wraps the first administrator in a user envelope, and sends no plan', () => {
    service
      .create({
        firstName: 'Ada',
        lastName: 'Lovelace',
        emailAddress: 'ada@example.com',
        password: 'correct-horse-8',
        repeatPassword: 'correct-horse-8',
      })
      .subscribe();
    const request = http.expectOne(scoped(`${BASE}/create`));
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      user: {
        firstName: 'Ada',
        lastName: 'Lovelace',
        emailAddress: 'ada@example.com',
        password: 'correct-horse-8',
        repeatPassword: 'correct-horse-8',
      },
    });
    request.flush({});
  });

  /*
   * `changePassword`, not `password`. `UserAccountServiceImpl.changePassword` forwards only
   * `getChangePassword()`; seller-ui sent the other field, so the password would have been null even
   * if the request had reached the right user — which it did not.
   */
  it('sends the new password under changePassword, keyed on the org id', () => {
    service.changeOwnerPassword(ORG, 'Passw0rd').subscribe();
    const request = http.expectOne((it) => it.url === `${BASE}/change-password`);
    expect(request.request.method).toBe('POST');
    expect(request.request.params.get('id')).toBe(ORG);
    expect(request.request.body).toEqual({changePassword: 'Passw0rd'});
    request.flush(null);
  });

  it('pages an org’s stores by org id', () => {
    service.stores(ORG, 0, 20).subscribe();
    const request = http.expectOne((it) => it.url === `${BASE}/stores`);
    expect(request.request.params.get('id')).toBe(ORG);
    expect(request.request.params.get('count')).toBe('20');
    request.flush(emptyPage);
  });

  /*
   * The four lifecycle operations. All POSTs with **query parameters and no body** — passing the name
   * or the reason in a body binds nothing, because the server declares them `@RequestParam`.
   */
  it('renames through query parameters, not a body', () => {
    service.rename(ORG, 'Nordwerk').subscribe();
    const request = http.expectOne((it) => it.url === `${BASE}/rename`);
    expect(request.request.method).toBe('POST');
    expect(request.request.params.get('id')).toBe(ORG);
    expect(request.request.params.get('name')).toBe('Nordwerk');
    expect(request.request.body).toBeNull();
    request.flush({});
  });

  it('sends a suspension reason only when there is one', () => {
    service.suspend(ORG, 'non-payment').subscribe();
    const withReason = http.expectOne((it) => it.url === `${BASE}/suspend`);
    expect(withReason.request.params.get('reason')).toBe('non-payment');
    withReason.flush({});

    // Omitted rather than sent empty: the server defaults it to "suspended by operator", and an
    // empty string would be recorded in the audit row as the operator's stated reason.
    service.suspend(ORG).subscribe();
    const without = http.expectOne((it) => it.url === `${BASE}/suspend`);
    expect(without.request.params.has('reason')).toBeFalse();
    without.flush({});
  });

  it('resumes and closes by id', () => {
    service.resume(ORG).subscribe();
    http.expectOne((it) => it.url === `${BASE}/resume` && it.params.get('id') === ORG).flush({});

    service.close(ORG).subscribe();
    http.expectOne((it) => it.url === `${BASE}/close` && it.params.get('id') === ORG).flush({});
  });
});
