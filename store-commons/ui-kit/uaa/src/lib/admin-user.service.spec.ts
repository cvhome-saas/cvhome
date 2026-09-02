import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@cvhome-saas/ui-kit';
import {AdminUserService} from './admin-user.service';

/*
 * The api tier's contract with the backend, and this one has more of it than most: two verbs that
 * differ from their tenancy equivalents (`PUT` for reset-password, a bare array body for roles), a
 * bracketed query-parameter syntax the server parses by hand, and a path prefix the gateway did not
 * route at all until this module.
 */
describe('AdminUserService', () => {
  let service: AdminUserService;
  let http: ReturnType<typeof apiHarness<AdminUserService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(AdminUserService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const BASE = '/uaa/api/v1/admin/users';
  const USER = 'c0ffee00-dead-4bee-8000-000000000001';

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;
  const emptyPage = {content: [], totalElements: 0, totalPages: 0, size: 20, number: 0};

  /*
   * `metadata[org]`, with the brackets literal: `AdminUserController.extractMetadataFilters` slices
   * the key by hand off `@RequestParam Map allParams`. A nested object or a dotted key matches
   * nothing and silently returns every user on the platform, which is the failure worth a spec.
   */
  it('sends metadata filters as bracketed query parameters', () => {
    service.list(0, 20, {org: '65f023632bc46470c104b76f'}).subscribe();
    const request = http.expectOne((it) => it.url === BASE);
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('metadata[org]')).toBe('65f023632bc46470c104b76f');
    expect(request.request.params.get('count')).toBe('20');
    expect(request.request.params.has('size')).toBeFalse();
    request.flush(emptyPage);
  });

  /** An empty filter is dropped rather than sent as `metadata[org]=`, which matches no user at all. */
  it('omits an empty filter instead of matching on the empty string', () => {
    service.list(0, 20, {org: ''}).subscribe();
    const request = http.expectOne((it) => it.url === BASE);
    expect(request.request.params.has('metadata[org]')).toBeFalse();
    request.flush(emptyPage);
  });

  it('reads and checks by id and username', () => {
    service.findOne(USER).subscribe();
    http.expectOne(scoped(`${BASE}/${USER}`)).flush({});

    service.usernameExists('ada').subscribe();
    const exists = http.expectOne((it) => it.url === `${BASE}/exists`);
    expect(exists.request.params.get('username')).toBe('ada');
    exists.flush(false);
  });

  it('creates and updates', () => {
    const created = {
      username: 'ada',
      email: 'ada@example.com',
      firstName: 'Ada',
      lastName: 'Lovelace',
      roles: ['STORE_ADMIN'],
      metadata: {org: '65f023632bc46470c104b76f'},
    };
    service.create(created).subscribe();
    const create = http.expectOne(scoped(BASE));
    expect(create.request.method).toBe('POST');
    expect(create.request.body).toEqual(created);
    create.flush({});

    service.update(USER, {enabled: false}).subscribe();
    const update = http.expectOne(scoped(`${BASE}/${USER}`));
    expect(update.request.method).toBe('PUT');
    expect(update.request.body).toEqual({enabled: false});
    update.flush({});
  });

  it('enables, disables and deletes by id', () => {
    service.enable(USER).subscribe();
    const enable = http.expectOne(scoped(`${BASE}/${USER}/enable`));
    expect(enable.request.method).toBe('POST');
    expect(enable.request.body).toBeNull();
    enable.flush(null);

    service.disable(USER).subscribe();
    http.expectOne(scoped(`${BASE}/${USER}/disable`)).flush(null);

    service.delete(USER).subscribe();
    const remove = http.expectOne(scoped(`${BASE}/${USER}`));
    expect(remove.request.method).toBe('DELETE');
    remove.flush(null);
  });

  /*
   * **A PUT, and the field is `password`.** tenancy's reset for the same underlying operation is a
   * POST carrying `UserPassword.changePassword`. Two spellings one hop apart is exactly the kind of
   * thing that is right in one file and wrong in the next.
   */
  it('resets a password with PUT and a password field', () => {
    service.resetPassword(USER, 'Passw0rd').subscribe();
    const request = http.expectOne(scoped(`${BASE}/${USER}/reset-password`));
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({password: 'Passw0rd'});
    request.flush(null);
  });

  /*
   * A bare JSON array, not `{roles: [...]}` — the server declares `@RequestBody Set<String>`. And
   * removal is a POST on its own path, not a DELETE.
   */
  it('assigns and removes roles as bare arrays', () => {
    service.assignRoles(USER, ['STORE_ADMIN']).subscribe();
    const assign = http.expectOne(scoped(`${BASE}/${USER}/roles`));
    expect(assign.request.method).toBe('POST');
    expect(assign.request.body).toEqual(['STORE_ADMIN']);
    assign.flush(null);

    service.removeRoles(USER, ['STORE_ADMIN']).subscribe();
    const remove = http.expectOne(scoped(`${BASE}/${USER}/roles/remove`));
    expect(remove.request.method).toBe('POST');
    expect(remove.request.body).toEqual(['STORE_ADMIN']);
    remove.flush(null);
  });

  it('reads the assignable roles', () => {
    service.assignableRoles().subscribe();
    http.expectOne(scoped(`${BASE}/assignable-roles`)).flush([]);
  });
});
