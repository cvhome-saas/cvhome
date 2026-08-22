/**
 * Ported from seller-ui/projects/seller-core/src/lib/models/user.ts, verified against
 * `store-commons/uaa-client`'s `UserEntity`/`ReadableUser`/`PersistableUser`/`UserPassword` and
 * tenancy's `OrgMemberDto`/`InvitationDto`/`CreatedInvitationDto`.
 *
 * Two services own what a console calls "a user":
 *
 * - **uaa** owns the account — `uaa.users` is `{id, username, email, first_name, last_name,
 *   password_hash, metadata jsonb, enabled}` and nothing else. Reached through tenancy's
 *   `UserAccountApi`, which adds the tenancy guard uaa does not have.
 * - **tenancy** owns membership — `tenancy.org_member` and `tenancy.org_invitation`.
 *
 * They do not share a key beyond uaa's id, which is why `OrgMember` carries a bare `userId` and the
 * console has to join the two itself.
 */

/**
 * A staff account as tenancy answers it.
 *
 * seller-core typed every field optional. These are not: the mapper
 * (`UserAccountServiceImpl.toReadableUser`) sets `id`, `userName`, `emailAddress`, `active` and
 * `roles` unconditionally from a `UserDto` whose corresponding columns are `NOT NULL`.
 *
 * `firstName` and `lastName` genuinely are nullable — the columns allow null and signup does not
 * require them.
 */
export interface ReadableUser {
  readonly id: string;
  readonly userName: string;
  readonly emailAddress: string;
  readonly firstName: string | null;
  readonly lastName: string | null;
  readonly active: boolean;
  readonly roles: readonly string[];
  /** uaa's `metadata.org`. Null for a principal confined to no organization. */
  readonly org: string | null;
  /**
   * uaa's `metadata.store`.
   *
   * **Null for an org admin**, and that is not an edge case — it is how the seeded `org1-admin` is
   * stored. `UserAccountApi.list` filters uaa on `{org, store}`, so a user with no store appears in
   * no store's list, and `validateUserAccess` refuses `find-one` for them under any store at all.
   * See lessons.md, "Users — the user list is store-scoped, so an org admin is in no list".
   */
  readonly store: string | null;
}

/**
 * A staff account as tenancy accepts it.
 *
 * `org` and `store` are deliberately absent: `ManagedUserAccountServiceImpl` overwrites both from
 * the caller's own identity on create *and* on update, precisely so a user cannot be moved into
 * another tenant by editing a payload. Sending them would be sending a value the server discards.
 *
 * `repeatPassword` is likewise absent — see the note on `UserPassword` below.
 */
export interface PersistableUser {
  /** Absent when creating. uaa assigns it. */
  id?: string;
  userName: string;
  emailAddress: string;
  firstName: string | null;
  lastName: string | null;
  active: boolean;
  roles: string[];
  /** Only on create. `update` does not change a password; `reset` is the endpoint that does. */
  password?: string;
}

/**
 * The body of `POST …/user-account/reset`.
 *
 * `UserPassword` on the Java side has two fields, `password` and `changePassword`, and
 * **`password` is read by nothing**: `UserAccountServiceImpl.changePassword` sends only
 * `getChangePassword()` on to uaa's admin reset. There is no current-password verification anywhere
 * on the platform, so the console does not ask for one — a field nothing checks is a fixture
 * standing in for a real answer. See lessons.md, "Users — no self-service password change".
 */
export interface UserPassword {
  changePassword: string;
}

/** Mirrors tenancy's `InvitationStatus`. */
export type InvitationStatus = 'PENDING' | 'ACCEPTED' | 'REVOKED' | 'EXPIRED';

/**
 * Someone who belongs to an organization — tenancy's `OrgMemberDto`.
 *
 * Carries **only** uaa's id, with no name and no email, so a member list that shows people needs a
 * second lookup against `user-account`. The console does not currently need one: the Team tab reads
 * `user-account/list`, which already carries the person, and this exists for the invitation flow's
 * "did they actually join" answer.
 */
export interface OrgMember {
  readonly orgId: string;
  readonly userId: string;
  readonly role: string;
  readonly addedAt: string;
  readonly addedBy: string;
}

/** An invitation as it is safe to read — deliberately **without** the token. */
export interface Invitation {
  readonly id: string;
  readonly orgId: string;
  readonly email: string;
  readonly role: string;
  readonly status: InvitationStatus;
  readonly expiresAt: string;
  readonly createdAt: string;
  readonly createdBy: string;
}

/**
 * A freshly created invitation, and the only place its token is ever readable.
 *
 * Only the hash is stored, so this response cannot be reconstructed afterwards: losing it means
 * issuing a new invitation. **Nothing on the platform sends email**, which is why the console has to
 * show the link rather than say "we've emailed them" — see lessons.md, "Users — nothing emails an
 * invitation".
 */
export interface CreatedInvitation {
  readonly invitation: Invitation;
  readonly token: string;
}
