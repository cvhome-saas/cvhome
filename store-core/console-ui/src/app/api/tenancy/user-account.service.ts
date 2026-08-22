/** Ported from seller-ui/projects/seller-core/src/lib/auth/user.service.ts. */
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {SpringPage} from '@core/table/table.types';
import type {PersistableUser, ReadableUser, UserPassword} from '@models/users';

/**
 * Ported from seller-ui/projects/seller-core/src/lib/auth/user.service.ts, verified against
 * tenancy's `UserAccountApi`.
 *
 * Staff accounts: uaa is the store of record, tenancy is the tenant guard in front of it. uaa
 * enforces no tenancy of its own, so `ManagedUserAccountServiceImpl.validateUserAccess` deciding
 * "is this user yours" on every call is the only thing standing between one organization and
 * another's user records.
 *
 * **Three of seller-core's methods are not ported.**
 *
 * - `getCurrentAccount()` → `GET …/user-account/current`. It 500s for every caller — it binds
 *   `@AuthenticationPrincipal Principal` where the principal is a `Jwt` — and fixing that binding
 *   would only turn the 500 into a 404, because the id it would then pass is the *username* while
 *   the lookup behind it is uaa's by-UUID one. Both routes to the signed-in user's own record are
 *   closed. See lessons.md, "Users — the JWT carries no user id".
 * - `updatePassword()` → `PATCH /v1/private/user/{id}/password`. No such mapping exists anywhere in
 *   the platform; seller-core's own comment says so. `reset` below is the endpoint that works.
 * - `getUserId()`, which read `localStorage['userId']` — a key nothing in seller-ui ever writes.
 */
export const USER_ACCOUNT_API_BASE = '/tenancy/api/v1/user-account';

@Injectable({providedIn: 'root'})
export class UserAccountService {
  private readonly crudService = inject(CrudService);

  /**
   * A page of the open store's staff.
   *
   * `count`, not Spring's `size`: `tenancy-service` depends on `store-commons:autoconfigure`, whose
   * `ServletWebConfig` registers a `PageableHandlerMethodArgumentResolver` with
   * `setSizeParameterName("count")`, and `ManagedUserAccountServiceImpl.list` forwards
   * `pageable.getPageSize()` to uaa intact. seller-ui sends `count` while rendering ten rows a page
   * and assumes the server ignored it; it does not.
   *
   * `?store=` is stamped by `CrudService` from the request context, so it is never passed here.
   * **The filter behind it is `{org, store}` equality on uaa's metadata**, which is why there is no
   * search parameter to add: uaa's admin list matches metadata and nothing else — no name, no
   * email, no username. See lessons.md, "Users — no user search of any kind".
   */
  list(page: number, count: number): Observable<SpringPage<ReadableUser>> {
    return this.crudService.get(`${USER_ACCOUNT_API_BASE}/list`, {page, count});
  }

  /**
   * One user, guarded twice: the permission token, then the org and store comparison.
   *
   * `userId` is uaa's UUID, taken from a `list` row. It is not the username, and it is not the JWT
   * `sub` — which *is* the username, which is why this cannot be used to read the caller's own row.
   */
  findOne(userId: string): Observable<ReadableUser> {
    return this.crudService.get(`${USER_ACCOUNT_API_BASE}/find-one`, {userId});
  }

  /**
   * The roles this console may assign.
   *
   * Answers uaa's whole role table minus `USER` and `ORG_ADMIN` — which means it offers
   * **`SUPER_ADMIN`** to an org admin. The console filters that out before it reaches a picker; that
   * is defence in depth and not a fix, and the server should not be offering it. See lessons.md,
   * "Users — assignable-roles offers SUPER_ADMIN to an org admin".
   */
  assignableRoles(): Observable<string[]> {
    return this.crudService.get(`${USER_ACCOUNT_API_BASE}/assignable-roles`);
  }

  /**
   * Creates a staff account in the open store.
   *
   * **Not atomic.** `UserAccountServiceImpl.createUser` calls uaa twice — create, then set the
   * password — and the first has already committed if the second fails, leaving an account that
   * exists and cannot be signed into. See lessons.md, "Users — creating a user is two calls".
   *
   * The server overwrites `org` and `store` from the caller's identity, so neither is sent.
   */
  create(user: PersistableUser): Observable<ReadableUser> {
    return this.crudService.post(`${USER_ACCOUNT_API_BASE}/create`, user);
  }

  /** Updates name, email, roles and the active flag. Does not touch the password — `reset` does. */
  update(user: PersistableUser): Observable<ReadableUser> {
    return this.crudService.put(`${USER_ACCOUNT_API_BASE}/update`, user);
  }

  /**
   * Sets another user's password.
   *
   * Refused with a 403 for **every** caller until `STORE-CORE.USERS.RESET_PASSWORD` was given a case
   * in `CustomPermissionEvaluator` — the token was declared on the endpoint and matched nowhere, so
   * it fell through every switch to `default -> false`. Fixed in
   * `fix(commons): the permission token that locked every password reset`.
   *
   * There is no current-password check: the body's second field is read by nothing. See the note on
   * `UserPassword`.
   */
  resetPassword(userId: string, password: UserPassword): Observable<void> {
    return this.crudService.post(`${USER_ACCOUNT_API_BASE}/reset`, password, {userId});
  }

  /** Removes the account from uaa outright. Refused for the super administrator. */
  delete(userId: string): Observable<void> {
    return this.crudService.delete(`${USER_ACCOUNT_API_BASE}/delete`, {userId});
  }

  /**
   * Lets a user sign in again.
   *
   * seller-core posted an `undefined` body here and passed `store` explicitly, which `CrudService`
   * then overwrote from the request context. Neither is done.
   */
  enable(userId: string): Observable<void> {
    return this.crudService.post(`${USER_ACCOUNT_API_BASE}/enable`, {}, {userId});
  }

  /** Stops a user signing in, without removing what they did. */
  disable(userId: string): Observable<void> {
    return this.crudService.post(`${USER_ACCOUNT_API_BASE}/disable`, {}, {userId});
  }
}
