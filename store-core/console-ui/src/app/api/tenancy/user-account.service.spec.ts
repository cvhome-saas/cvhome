import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import type {ReadableUser} from '@models/users';
import {USER_ACCOUNT_API_BASE, UserAccountService} from './user-account.service';

/*
 * The api tier's contract with tenancy: a path, a verb, a parameter name, a body shape. None of it
 * is checked by the compiler — a wrong path is still a string.
 */
describe('UserAccountService', () => {
  let service: UserAccountService;
  let http: ReturnType<typeof apiHarness<UserAccountService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(UserAccountService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const USER_ID = '60AB49A5-7F06-4B5A-BE81-9B30BB6559AE';

  const user: ReadableUser = {
    id: USER_ID,
    userName: 'org1-store1-admin',
    emailAddress: 'org1-store1-admin@mail.com',
    firstName: 'Store1',
    lastName: 'Admin',
    active: true,
    roles: ['STORE_ADMIN'],
    org: '21f023932bc66470c104b76f',
    store: TEST_STORE,
  };

  const page = {content: [user], size: 20, totalElements: 1, totalPages: 1, number: 0};

  /*
   * `count`, not Spring's `size`. tenancy depends on store-commons:autoconfigure, whose
   * ServletWebConfig renames the page-size parameter platform-wide, and this is the assertion that
   * would notice if that ever stopped being true.
   */
  it('pages the list with count, not size, and scopes it to the open store', () => {
    service.list(1, 25).subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${USER_ACCOUNT_API_BASE}/list`);
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('page')).toBe('1');
    expect(request.request.params.get('count')).toBe('25');
    expect(request.request.params.has('size')).toBe(false);
    expect(request.request.params.get('store')).toBe(TEST_STORE);
    request.flush(page);
  });

  it('reads one user by uaa id', () => {
    service.findOne(USER_ID).subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${USER_ACCOUNT_API_BASE}/find-one`);
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('userId')).toBe(USER_ID);
    request.flush(user);
  });

  it('reads the assignable roles', () => {
    service.assignableRoles().subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${USER_ACCOUNT_API_BASE}/assignable-roles`);
    expect(request.request.method).toBe('GET');
    request.flush(['STORE_ADMIN', 'STORE_MODERATOR', 'SUPER_ADMIN']);
  });

  /*
   * `org` and `store` are never sent: the server overwrites both from the caller's own identity on
   * create and on update, precisely so a user cannot be moved into another tenant by editing a
   * payload. Sending them would be sending a value the server discards.
   */
  it('creates a user without naming a tenant', () => {
    service.create({
      userName: 'newbie',
      emailAddress: 'newbie@example.com',
      firstName: 'New',
      lastName: 'Bie',
      active: true,
      roles: ['STORE_MODERATOR'],
      password: 'Passw0rd1',
    }).subscribe();

    const request = http.expectOne((candidate) => candidate.url === `${USER_ACCOUNT_API_BASE}/create`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body.userName).toBe('newbie');
    expect(request.request.body.org).toBeUndefined();
    expect(request.request.body.store).toBeUndefined();
    request.flush(user);
  });

  it('updates a user with a PUT', () => {
    service.update({
      id: USER_ID,
      userName: 'org1-store1-admin',
      emailAddress: 'org1-store1-admin@mail.com',
      firstName: 'Store One',
      lastName: 'Admin',
      active: true,
      roles: ['STORE_ADMIN'],
    }).subscribe();

    const request = http.expectOne((candidate) => candidate.url === `${USER_ACCOUNT_API_BASE}/update`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body.id).toBe(USER_ID);
    request.flush(user);
  });

  /*
   * The endpoint the permission fix opened. The body carries only `changePassword`: `UserPassword`'s
   * other field is read by nothing on the Java side, so the console does not collect a current
   * password it cannot have verified.
   */
  it('resets a password with the id in the query and only changePassword in the body', () => {
    service.resetPassword(USER_ID, {changePassword: 'Passw0rd1'}).subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${USER_ACCOUNT_API_BASE}/reset`);
    expect(request.request.method).toBe('POST');
    expect(request.request.params.get('userId')).toBe(USER_ID);
    expect(request.request.params.get('store')).toBe(TEST_STORE);
    expect(request.request.body).toEqual({changePassword: 'Passw0rd1'});
    request.flush(null);
  });

  it('deletes a user', () => {
    service.delete(USER_ID).subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${USER_ACCOUNT_API_BASE}/delete`);
    expect(request.request.method).toBe('DELETE');
    expect(request.request.params.get('userId')).toBe(USER_ID);
    request.flush(null);
  });

  /*
   * seller-core posted an `undefined` body to both of these and passed `store` explicitly, which
   * CrudService then overwrote from the request context. Neither survives the port.
   */
  it('enables and disables with an empty body and the id in the query', () => {
    service.enable(USER_ID).subscribe();
    const enable = http.expectOne((candidate) => candidate.url === `${USER_ACCOUNT_API_BASE}/enable`);
    expect(enable.request.method).toBe('POST');
    expect(enable.request.body).toEqual({});
    expect(enable.request.params.get('userId')).toBe(USER_ID);
    enable.flush(null);

    service.disable(USER_ID).subscribe();
    const disable = http.expectOne((candidate) => candidate.url === `${USER_ACCOUNT_API_BASE}/disable`);
    expect(disable.request.method).toBe('POST');
    expect(disable.request.params.get('userId')).toBe(USER_ID);
    disable.flush(null);
  });
});
