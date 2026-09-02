import {TestBed} from '@angular/core/testing';
import {Observable, of, throwError} from 'rxjs';

import {OrgMemberService} from '@api/tenancy/org-member.service';
import {UserAccountService} from '@api/tenancy/user-account.service';
import type {SpringPage} from '@cvhome-saas/ui-kit';
import type {Invitation, PersistableUser, ReadableUser, UserPassword} from '@models/users';
import {UsersApi} from './users.api.service';

/** A store-scoped account, exactly as `ReadableUser` sends one. */
const STORE_ADMIN: ReadableUser = {
  id: '60AB49A5-7F06-4B5A-BE81-9B30BB6559AE',
  userName: 'org1-store1-admin',
  emailAddress: 'org1-store1-admin@mail.com',
  firstName: 'Store1',
  lastName: 'Admin',
  active: true,
  roles: ['STORE_ADMIN'],
  org: '21f023932bc66470c104b76f',
  store: 'ORG1-STORE1',
};

const MODERATOR: ReadableUser = {
  ...STORE_ADMIN,
  id: '0C1C7C69-F504-47E2-AA5D-3348CBD1023F',
  userName: 'org1-store1-moderator',
  emailAddress: 'org1-store1-moderator@mail.com',
  firstName: null,
  lastName: null,
  active: false,
  roles: ['STORE_MODERATOR'],
};

function page(content: ReadableUser[]): SpringPage<ReadableUser> {
  return {content, totalElements: content.length, totalPages: 1, size: 20, number: 0};
}

class FakeUserAccountService {
  listed: {page: number; count: number}[] = [];
  users: ReadableUser[] = [STORE_ADMIN, MODERATOR];
  /** What uaa actually answers: its whole role table minus `USER` and `ORG_ADMIN`. */
  roles: string[] = ['STORE_ADMIN', 'STORE_MODERATOR', 'SUPER_ADMIN'];
  rolesFail = false;
  created: PersistableUser[] = [];
  updated: PersistableUser[] = [];
  resets: {userId: string; body: UserPassword}[] = [];
  enabled: string[] = [];
  disabled: string[] = [];
  deleted: string[] = [];

  list(pageNumber: number, count: number): Observable<SpringPage<ReadableUser>> {
    this.listed.push({page: pageNumber, count});
    return of(page(this.users));
  }

  assignableRoles(): Observable<string[]> {
    return this.rolesFail ? throwError(() => new Error('uaa is down')) : of(this.roles);
  }

  create(user: PersistableUser): Observable<ReadableUser> {
    this.created.push(user);
    return of(STORE_ADMIN);
  }

  update(user: PersistableUser): Observable<ReadableUser> {
    this.updated.push(user);
    return of(STORE_ADMIN);
  }

  resetPassword(userId: string, body: UserPassword): Observable<void> {
    this.resets.push({userId, body});
    return of(undefined);
  }

  enable(userId: string): Observable<void> {
    this.enabled.push(userId);
    return of(undefined);
  }

  disable(userId: string): Observable<void> {
    this.disabled.push(userId);
    return of(undefined);
  }

  delete(userId: string): Observable<void> {
    this.deleted.push(userId);
    return of(undefined);
  }
}

const PENDING: Invitation = {
  id: 'inv-1',
  orgId: '21f023932bc66470c104b76f',
  email: 'newbie@example.com',
  role: 'STORE_ADMIN',
  status: 'PENDING',
  expiresAt: '2026-09-01T00:00:00Z',
  createdAt: '2026-08-22T00:00:00Z',
  createdBy: 'org1-admin',
};

class FakeOrgMemberService {
  list: Invitation[] = [PENDING, {...PENDING, id: 'inv-2', status: 'ACCEPTED'}];
  invited: {email: string; role: string}[] = [];

  invitations(): Observable<Invitation[]> {
    return of(this.list);
  }

  invite(email: string, role: string): Observable<{invitation: Invitation; token: string}> {
    this.invited.push({email, role});
    return of({invitation: PENDING, token: 'once-only'});
  }

  resend(email: string, role: string): Observable<{invitation: Invitation; token: string}> {
    this.invited.push({email, role});
    return of({invitation: PENDING, token: 'rotated'});
  }

  revoke(): Observable<Invitation> {
    return of({...PENDING, status: 'REVOKED'});
  }
}

describe('UsersApi', () => {
  let api: UsersApi;
  let users: FakeUserAccountService;
  let members: FakeOrgMemberService;

  beforeEach(() => {
    users = new FakeUserAccountService();
    members = new FakeOrgMemberService();
    TestBed.configureTestingModule({
      providers: [
        {provide: UserAccountService, useValue: users},
        {provide: OrgMemberService, useValue: members},
      ],
    });
    api = TestBed.inject(UsersApi);
  });

  const query = {page: {page: 0, count: 20}, self: 'org1-store1-admin'};

  it('maps a user onto a row, joining the name and keeping the parts', async () => {
    const snapshot = await firstValue(api.loadTeam(query));
    const [admin] = snapshot.rows;

    expect(admin.name).toBe('Store1 Admin');
    expect(admin.firstName).toBe('Store1');
    expect(admin.lastName).toBe('Admin');
    expect(admin.email).toBe('org1-store1-admin@mail.com');
    expect(admin.active).toBeTrue();
  });

  /* A person with no first or last name is not a blank row — the username is what identifies them. */
  it('falls back to the username when neither name is set', async () => {
    const snapshot = await firstValue(api.loadTeam(query));

    expect(snapshot.rows[1].name).toBe('org1-store1-moderator');
  });

  /*
   * The only identity a row and a token share: the JWT `sub` is the username, not uaa's id, so
   * there is no id to compare. See lessons.md, "Users — the JWT carries no user id".
   */
  it('marks the signed-in operator by username, since there is no id to match on', async () => {
    const snapshot = await firstValue(api.loadTeam(query));

    expect(snapshot.rows[0].isSelf).toBeTrue();
    expect(snapshot.rows[1].isSelf).toBeFalse();
  });

  /*
   * The endpoint offers platform superuser to an org admin. The console intersects rather than
   * filters one name, so a role added to uaa's table later cannot appear unreviewed either.
   */
  it('never offers SUPER_ADMIN, or anything the console has not reviewed', async () => {
    users.roles = ['STORE_ADMIN', 'STORE_MODERATOR', 'SUPER_ADMIN', 'SOMETHING_NEW'];

    const snapshot = await firstValue(api.loadTeam(query));

    expect(snapshot.assignableRoles).toEqual(['STORE_MODERATOR', 'STORE_ADMIN']);
  });

  /* The roles lookup is an optional leg; the list is the page and is not wrapped. */
  it('still answers when the role lookup fails', async () => {
    users.rolesFail = true;

    const snapshot = await firstValue(api.loadTeam(query));

    expect(snapshot.rows.length).toBe(2);
    expect(snapshot.assignableRoles).toEqual([]);
  });

  it('pages with count and reports the server total', async () => {
    const snapshot = await firstValue(api.loadTeam({...query, page: {page: 2, count: 20}}));

    expect(users.listed).toEqual([{page: 2, count: 20}]);
    expect(snapshot.totalElements).toBe(2);
  });

  it('counts only the pending invitations', async () => {
    const snapshot = await firstValue(api.loadInvitations());

    expect(snapshot.rows.length).toBe(2);
    expect(snapshot.pending).toBe(1);
    expect(snapshot.rows[0].pending).toBeTrue();
    expect(snapshot.rows[1].pending).toBeFalse();
  });

  it('sends only changePassword, never a current password nothing verifies', async () => {
    await firstValue(api.resetPassword('user-1', 'Passw0rd1'));

    expect(users.resets).toEqual([{userId: 'user-1', body: {changePassword: 'Passw0rd1'}}]);
  });

  /* One toggle on the page, two endpoints behind it. */
  it('routes the active toggle to enable or disable', async () => {
    await firstValue(api.setActive('user-1', true));
    await firstValue(api.setActive('user-2', false));

    expect(users.enabled).toEqual(['user-1']);
    expect(users.disabled).toEqual(['user-2']);
  });

  it('reads the one-time token off an invitation', async () => {
    const issued = await firstValue(api.invite('newbie@example.com', 'STORE_ADMIN'));

    expect(issued.token).toBe('once-only');
    expect(members.invited).toEqual([{email: 'newbie@example.com', role: 'STORE_ADMIN'}]);
  });
});

function firstValue<T>(source: Observable<T>): Promise<T> {
  return new Promise((resolve, reject) => source.subscribe({next: resolve, error: reject}));
}
