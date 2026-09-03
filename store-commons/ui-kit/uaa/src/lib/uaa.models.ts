/**
 * Console-native; no seller-core original — uaa's admin API has never had a caller.
 *
 * uaa's own view of an account, which is a different and *wider* record than tenancy's
 * `user-account/list` answers: that one is filtered to `{org, store}` metadata and is the open
 * store's team, while these are every account on the platform.
 *
 * Mirrors `uaa/dto/UserDto` and the three request records beside it. Two shapes to know:
 *
 * - `id` is a **UUID**, unlike `ManagerOrgId` and `PodId`, which are 24-char ObjectIds wrapped in
 *   `{id}`. `AdminUserController` declares `@PathVariable UUID id`, so a non-UUID does not even bind.
 * - `metadata` is `Map<String, Object>` — an open bag uaa neither validates nor documents. Two keys
 *   are written by `UserAccountServiceImpl`: `org` and `store`, both id strings. Everything reading
 *   it here goes through {@link metadataString}, because a value that is not a string is possible
 *   and must not take a table down.
 */

/** The two metadata keys `UserAccountServiceImpl` writes. Anything else in the bag is another service's. */
export const USER_METADATA_ORG = 'org';
export const USER_METADATA_STORE = 'store';

/**
 * Derived on the server, never stored: `DISABLED` when an administrator switched the account off, `LOCKED`
 * while a lockout holds, `PENDING` when it has never had a password, `ACTIVE` otherwise.
 */
export type UserStatus = 'ACTIVE' | 'PENDING' | 'LOCKED' | 'DISABLED';

export interface UserDto {
  /** uaa's UUID. Not the username, which is what a JWT `sub` carries. */
  readonly id: string;
  readonly username: string;
  readonly email: string | null;
  readonly firstName: string | null;
  readonly lastName: string | null;
  readonly enabled: boolean;
  readonly status: UserStatus;
  readonly emailVerified: boolean;
  readonly roles: readonly string[];
  /** `Map<String, Object>`: never assume a member is a string — read it with {@link metadataString}. */
  readonly metadata: Readonly<Record<string, unknown>>;
  readonly lastSignInAt: string | null;
  readonly lastSignInClientId: string | null;
  /** `PASSWORD`, or `IDP:<alias>` once brokered logins exist. */
  readonly lastSignInVia: string | null;
  readonly lockedUntil: string | null;
  readonly failedLoginAttempts: number;
  readonly passwordChangedAt: string | null;
  readonly createdAt: string | null;
}

/** One live session of an account, as `SessionSummary` on the server. */
export interface SessionSummary {
  readonly id: string;
  readonly createdAt: string;
  readonly lastAccessedAt: string;
  readonly expiresAt: string;
  readonly ip: string | null;
  readonly userAgent: string | null;
  readonly via: string | null;
  /** The caller's own session, where the caller has one on uaa. */
  readonly current: boolean;
}

/**
 * What `POST /uaa/api/v1/admin/users` takes.
 *
 * `password` is optional and goes through the realm's policy. Without it the account exists, is
 * enabled, and cannot sign in until a password is set through `reset-password` — which is how
 * tenancy's `UserAccountServiceImpl.createUser` still does it in two calls.
 */
export interface CreateUserRequest {
  readonly username: string;
  readonly email: string;
  readonly firstName: string | null;
  readonly lastName: string | null;
  readonly password?: string | null;
  readonly roles: readonly string[];
  readonly metadata: Readonly<Record<string, string>>;
}

/**
 * What `PUT /uaa/api/v1/admin/users/{id}` takes. Every field is optional: it is a partial update.
 * `metadata` merges key by key, and a key sent with `null` is **removed**. `email`, when present,
 * replaces the address and marks it unverified again; the username is the identity and stays.
 */
export interface UpdateUserRequest {
  readonly firstName?: string | null;
  readonly lastName?: string | null;
  readonly email?: string;
  readonly enabled?: boolean;
  readonly roles?: readonly string[];
  readonly metadata?: Readonly<Record<string, string | null>>;
}

/** `ResetUserPasswordRequest` — one field, and it is `password`, not `changePassword`. */
export interface ResetUserPasswordRequest {
  readonly password: string;
}

/**
 * One member of the metadata bag, when it is a string, and `null` otherwise.
 *
 * The Java side is `Map<String, Object>` and nothing constrains what goes in it, so a caller that
 * read `metadata['org'] as string` would be asserting rather than checking. A number, an object or
 * an absent key all answer `null` here, which every call site renders as "unknown".
 */
export function metadataString(
  metadata: Readonly<Record<string, unknown>> | null | undefined,
  key: string,
): string | null {
  const value = metadata?.[key];
  return typeof value === 'string' && value !== '' ? value : null;
}

/** `GET /users/counts`: the tiles above the account list. `locked` and `disabled` are subsets of `total`. */
export interface UserCounts {
  readonly total: number;
  readonly active: number;
  readonly pending: number;
  readonly locked: number;
  readonly disabled: number;
}

/**
 * The account list's filters. Every part is optional and they combine with AND.
 *
 * `q` is a case-insensitive contains over username, email and the full name; `metadata` is the
 * equality filter tenancy has always used (`metadata[org]=…`).
 */
export interface UserSearch {
  readonly q?: string;
  readonly status?: UserStatus | '';
  readonly role?: string;
  readonly metadata?: Readonly<Record<string, string>>;
}

export type InvitationStatus = 'PENDING' | 'ACCEPTED' | 'REVOKED' | 'EXPIRED';

/** An invitation as the console lists it. Never carries the token. */
export interface InvitationDto {
  readonly id: string;
  readonly userId: string;
  readonly username: string;
  readonly email: string;
  readonly status: InvitationStatus;
  readonly expiresAt: string;
  readonly createdAt: string;
  readonly createdBy: string | null;
  readonly acceptedAt: string | null;
}

/**
 * `POST /users/invitations`. The account is created pending — no password, not yet activated —
 * and a one-time link is issued for its first password. `username` defaults to the email.
 */
export interface InviteUserRequest {
  readonly username?: string | null;
  readonly email: string;
  readonly firstName: string | null;
  readonly lastName: string | null;
  readonly roles: readonly string[];
  readonly metadata: Readonly<Record<string, string>>;
}

/**
 * The one response that carries a one-time link. Shown once by the console; no later read can
 * reproduce it, because only the token's hash is stored. `invitation` is null for a reset link.
 */
export interface IssuedLink {
  readonly user: UserDto;
  readonly invitation: InvitationDto | null;
  readonly link: string;
  readonly expiresAt: string;
}

/** `POST /users/{id}/password-reset-links`: whether issuing the link also signs the account out everywhere. */
export interface CreateResetLinkRequest {
  readonly revokeSessions: boolean;
}

/** The realm's password rules, as the public accept page needs them to validate before the round trip. */
export interface PasswordRules {
  readonly minLength: number;
  readonly requireUpper: boolean;
  readonly requireLower: boolean;
  readonly requireDigit: boolean;
  readonly requireSpecial: boolean;
}

/**
 * What the public accept page shows before a password is chosen: whose account this is and what
 * the password must satisfy. Nothing else, because the page is reachable by whoever holds the link.
 */
export interface LinkPreview {
  readonly kind: 'INVITATION' | 'PASSWORD_RESET';
  readonly username: string;
  readonly email: string | null;
  readonly firstName: string | null;
  readonly expiresAt: string;
  readonly password: PasswordRules;
}

/** The accept page's answer: who may now sign in, and where. */
export interface AcceptedLink {
  readonly username: string;
  readonly loginUrl: string;
}
