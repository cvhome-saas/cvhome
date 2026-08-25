import {Injectable, inject} from '@angular/core';
import {Observable, forkJoin, map} from 'rxjs';

import {OrgMemberService} from '@api/tenancy/org-member.service';
import {UserAccountService} from '@api/tenancy/user-account.service';
import {optionalList} from '@core/http/optional';
import type {PageRequest} from '@core/table/table.types';
import {INVITATION_TONE, OFFERABLE_ROLES, type InvitationRow, type TeamRow} from '@models/team';
import type {Invitation, PersistableUser, ReadableUser} from '@models/users';

/** What the page asks the team tab for: a page, and who is asking. */
export interface TeamQuery {
  readonly page: PageRequest;
  /** The signed-in username, so a row can know it is the operator's own. */
  readonly self: string;
}

/** Everything the team tab renders for one query. */
export interface TeamSnapshot {
  readonly rows: readonly TeamRow[];
  readonly totalElements: number;
  readonly totalPages: number;
  /**
   * The roles a picker may offer: the server's answer intersected with `OFFERABLE_ROLES`.
   *
   * Empty when the lookup failed, which the form reads as "cannot choose a role right now" rather
   * than as "this user has no roles" — the leg is optional, the page is not.
   */
  readonly assignableRoles: readonly string[];
}

/** Everything the invitations tab renders. */
export interface InvitationsSnapshot {
  readonly rows: readonly InvitationRow[];
  readonly pending: number;
}

/**
 * The user page's data.
 *
 * Two tabs over two services that share no key: uaa owns the account and tenancy owns the
 * membership, joined only by uaa's id. That is why the team tab reads `user-account/list` for people
 * and the invitations tab reads `org-member/invitations` for invitations, rather than one list
 * answering both.
 *
 * **The list has no search and no sort.** uaa's admin list matches on metadata equality — the `{org,
 * store}` pair tenancy scopes with — and offers no name, email or username query at all, so the
 * template's "Name or email" box has nothing to send. See lessons.md, "Users — no user search of any
 * kind".
 *
 * **The KPI counts are read off what is already loaded**, not fetched separately: the team count is
 * the list's own `totalElements`, and the pending-invitation count is a length. Neither costs a
 * request, which is why there is no separate counts key here as there is on the payments ledger.
 */
@Injectable({providedIn: 'root'})
export class UsersApi {
  private readonly users = inject(UserAccountService);
  private readonly members = inject(OrgMemberService);

  /**
   * A page of the open store's staff, with the roles a form may assign.
   *
   * The list is the **unwrapped leg that is the page** — a failure there must reach `error` so the
   * operator gets a retry, rather than showing an empty team as though the store had none. The role
   * lookup is `optionalList`, because a form that falls back to showing only the roles a user
   * already has is still a working form.
   */
  loadTeam(query: TeamQuery): Observable<TeamSnapshot> {
    return forkJoin({
      page: this.users.list(query.page.page, query.page.count),
      // Optional: a form that cannot offer new roles still edits a name, and the picker falls back
      // to the roles the user already holds.
      roles: this.users.assignableRoles().pipe(optionalList()),
    }).pipe(
      map(({page, roles}): TeamSnapshot => ({
        rows: (page.content ?? []).map((user) => toTeamRow(user, query.self)),
        totalElements: page.totalElements,
        totalPages: page.totalPages,
        assignableRoles: offerable(roles),
      })),
    );
  }

  /**
   * Every invitation the organization has issued.
   *
   * Unpaged, because the endpoint is: `OrgMemberApi.invitations` answers a `List`, not a page. An
   * organization accumulates one row per address ever invited, so this will not stay small forever
   * — noted rather than worked around, since inventing client-side paging over a list the server
   * sends whole would hide the growth rather than fix it.
   */
  loadInvitations(): Observable<InvitationsSnapshot> {
    return this.members.invitations().pipe(
      map((invitations): InvitationsSnapshot => ({
        rows: invitations.map(toInvitationRow),
        pending: invitations.filter((invitation) => invitation.status === 'PENDING').length,
      })),
    );
  }

  create(user: PersistableUser): Observable<void> {
    return this.users.create(user).pipe(map(() => undefined));
  }

  update(user: PersistableUser): Observable<void> {
    return this.users.update(user).pipe(map(() => undefined));
  }

  resetPassword(userId: string, password: string): Observable<void> {
    return this.users.resetPassword(userId, {changePassword: password});
  }

  /** `enable` and `disable` are two endpoints; the page has one toggle. */
  setActive(userId: string, active: boolean): Observable<void> {
    return active ? this.users.enable(userId) : this.users.disable(userId);
  }

  delete(userId: string): Observable<void> {
    return this.users.delete(userId);
  }

  invite(email: string, role: string): Observable<{token: string; expiresAt: string}> {
    return this.members.invite(email, role).pipe(map(toIssued));
  }

  resend(email: string, role: string): Observable<{token: string; expiresAt: string}> {
    return this.members.resend(email, role).pipe(map(toIssued));
  }

  revoke(invitationId: string): Observable<void> {
    return this.members.revoke(invitationId).pipe(map(() => undefined));
  }
}

/* --------------------------------------------------------------------------- shaping ---- */

/**
 * The server's role list, narrowed to what the console will offer.
 *
 * An intersection rather than a filter of one name: it drops `SUPER_ADMIN`, which the server should
 * not be offering an org admin, *and* anything else uaa's role table gains that nobody has reviewed
 * — a role is a database row here, not an enum. See lessons.md, "Users — assignable-roles offers
 * SUPER_ADMIN to an org admin".
 */
function offerable(roles: readonly string[]): readonly string[] {
  return OFFERABLE_ROLES.filter((role) => roles.includes(role));
}

/**
 * One account, as a table row.
 *
 * The columns the template designs and this cannot fill: an avatar photo, a phone number, a postal
 * address, "last active", a lifetime value and an order tally. `ReadableUser.lastAccess` and
 * `.loginTime` look like the third of those and are **dead fields** — declared on the DTO, set by no
 * mapper, backed by no column — which is why neither is read here. See lessons.md, "Users — a user
 * has no last-login, no avatar and no profile fields".
 *
 * `isSelf` matches on username because that is the only identity the two sides share: the JWT `sub`
 * is the username, not uaa's id, so there is no id to compare. See lessons.md, "Users — the JWT
 * carries no user id".
 */
function toTeamRow(user: ReadableUser, self: string): TeamRow {
  const name = [user.firstName, user.lastName].filter(Boolean).join(' ').trim();
  return {
    id: user.id,
    name: name || user.userName,
    firstName: user.firstName ?? '',
    lastName: user.lastName ?? '',
    userName: user.userName,
    email: user.emailAddress,
    roles: user.roles ?? [],
    active: user.active,
    store: user.store,
    isSelf: user.userName === self,
  };
}

function toInvitationRow(invitation: Invitation): InvitationRow {
  return {
    id: invitation.id,
    email: invitation.email,
    role: invitation.role,
    status: invitation.status,
    tone: INVITATION_TONE[invitation.status] ?? 'slate',
    expiresAt: invitation.expiresAt,
    createdAt: invitation.createdAt,
    createdBy: invitation.createdBy,
    pending: invitation.status === 'PENDING',
  };
}

function toIssued(created: {token: string; invitation: Invitation}): {token: string; expiresAt: string} {
  return {token: created.token, expiresAt: created.invitation.expiresAt};
}
