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

export interface UserDto {
  /** uaa's UUID. Not the username, which is what a JWT `sub` carries. */
  readonly id: string;
  readonly username: string;
  readonly email: string | null;
  readonly firstName: string | null;
  readonly lastName: string | null;
  readonly enabled: boolean;
  readonly roles: readonly string[];
  /** `Map<String, Object>`: never assume a member is a string — read it with {@link metadataString}. */
  readonly metadata: Readonly<Record<string, unknown>>;
}

/**
 * What `POST /uaa/api/v1/admin/users` takes.
 *
 * **No password field**, which is why creating an account through this API is two calls: the record
 * is `(username, email, firstName, lastName, roles, metadata)` and the password is set afterwards
 * through `reset-password`. tenancy's `UserAccountServiceImpl.createUser` does exactly that
 * internally — see lessons.md, "Users — creating a user is two calls".
 */
export interface CreateUserRequest {
  readonly username: string;
  readonly email: string;
  readonly firstName: string | null;
  readonly lastName: string | null;
  readonly roles: readonly string[];
  readonly metadata: Readonly<Record<string, string>>;
}

/** What `PUT /uaa/api/v1/admin/users/{id}` takes. Every field is nullable: it is a partial update. */
export interface UpdateUserRequest {
  readonly firstName?: string | null;
  readonly lastName?: string | null;
  readonly enabled?: boolean;
  readonly roles?: readonly string[];
  readonly metadata?: Readonly<Record<string, string>>;
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
