/** Console-native; no seller-core original — uaa's admin API has never had a caller. */
import {Injectable, inject} from '@angular/core';
import {Observable, concat, map, of, toArray} from 'rxjs';

import {CrudService, UI_KIT_CONFIG} from '@cvhome-saas/ui-kit';
import type {SpringPage} from '@cvhome-saas/ui-kit';
import type {CreateUserRequest, ResetUserPasswordRequest, UpdateUserRequest, UserDto} from './uaa.models';

/**
 * uaa's own user administration, addressed directly rather than through tenancy.
 *
 * **Two things are new about this file.** It is the first client for `AdminUserController`, which is
 * complete, guarded and was called by no frontend in the repository; and `/uaa/**` is a gateway route
 * that did not exist until Module 11 — `GatewayRouteLocatorImpl` listed `uaa` in the array it
 * *negates* to build the console's catch-all, and declared no forward route, so every path under it
 * matched nothing and 404'd.
 *
 * **Why this and not tenancy's `UserAccountApi`.** That one is store-scoped: it filters uaa on
 * `{org, store}` metadata, so an org admin — stored with no `store` — appears in no store's list at
 * all, including their own. This one is platform-wide, which is what a platform operator needs and
 * what makes the per-organization Users tab possible. See lessons.md, "Users — the user list is
 * store-scoped, so an org admin is in no list".
 *
 * **What guards it.** uaa's `AppSecurityConfig` gates `/api/v1/admin/**` on
 * `SCOPE_super_admin`/`ROLE_SUPER_ADMIN` at the filter chain *and* every method carries the same
 * `@PreAuthorize`. The gateway relays the operator's token unchanged; uaa's own guard, not the
 * gateway's, is what keeps this safe.
 *
 * **Ids here are UUIDs**, not the 24-character ObjectIds tenancy and the registry use — every path
 * variable is declared `@PathVariable UUID id`, so a wrong-shaped id is a 400 at binding rather than
 * a 404.
 */
export const ADMIN_USER_API_PATH = '/api/v1/admin/users';

@Injectable({providedIn: 'root'})
export class AdminUserService {
  private readonly crudService = inject(CrudService);
  /** `/uaa/…` behind the gateway, `/api/…` on uaa itself. See {@link UiKitConfig.uaaBasePath}. */
  private readonly base = `${inject(UI_KIT_CONFIG).uaaBasePath}${ADMIN_USER_API_PATH}`;

  /**
   * A page of accounts, optionally narrowed by metadata.
   *
   * **Metadata equality is the only filter there is.** `AdminService.getUsers` reads every
   * `metadata[<key>]` query parameter and matches on equality; there is no query over username,
   * email or name, so the console offers an organization filter and no search box. See lessons.md,
   * "Platform users — no text search".
   *
   * `count`, not Spring's `size` — uaa depends on `store-commons:autoconfigure` like everything else,
   * so the platform-wide rename applies here too. The controller takes `@RequestParam Map allParams`
   * and picks only the `metadata[...]` keys out of it, so the paging and the `?store=`/`?pod=` the
   * request context stamps are ignored rather than mistaken for filters.
   */
  list(page: number, count: number, metadata: Readonly<Record<string, string>> = {}): Observable<SpringPage<UserDto>> {
    const filters: Record<string, string> = {};
    for (const [key, value] of Object.entries(metadata)) {
      if (value) {
        filters[`metadata[${key}]`] = value;
      }
    }
    return this.crudService.get(this.base, {page, count, ...filters});
  }

  /** One account by uaa id. */
  findOne(id: string): Observable<UserDto> {
    return this.crudService.get(`${this.base}/${id}`);
  }

  /** Whether a username is taken. Answers a bare `true`/`false`, not an envelope. */
  usernameExists(username: string): Observable<boolean> {
    return this.crudService.get(`${this.base}/exists`, {username});
  }

  /**
   * Creates an account.
   *
   * **No password field**, so an account created here cannot be signed into until
   * {@link resetPassword} runs — the same two-call shape tenancy's create has internally, with the
   * same non-atomicity. See lessons.md, "Users — creating a user is two calls".
   */
  create(request: CreateUserRequest): Observable<UserDto> {
    return this.crudService.post(this.base, request);
  }

  /** A partial update: every field on `UpdateUserRequest` is nullable and null means "leave it". */
  update(id: string, request: UpdateUserRequest): Observable<UserDto> {
    return this.crudService.put(`${this.base}/${id}`, request);
  }

  /**
   * Enables an account.
   *
   * Refuses with `SuperAdminImmutableException` when the target is the platform's own super admin —
   * surfaced as the server's refusal rather than predicted, because which account that is belongs to
   * uaa's configuration and not to this client.
   */
  enable(id: string): Observable<void> {
    return this.crudService.post(`${this.base}/${id}/enable`, null);
  }

  /** Disables an account. Same super-admin refusal as {@link enable}. */
  disable(id: string): Observable<void> {
    return this.crudService.post(`${this.base}/${id}/disable`, null);
  }

  /** Deletes an account. Same super-admin refusal; there is no undo and no soft delete. */
  delete(id: string): Observable<void> {
    return this.crudService.delete(`${this.base}/${id}`);
  }

  /**
   * Sets a password. **`PUT`, and the field is `password`** — unlike tenancy's reset, which is a POST
   * carrying `UserPassword.changePassword`. Two spellings of the same operation, one hop apart.
   *
   * No current-password field, because nothing on the platform verifies one.
   */
  resetPassword(id: string, password: string): Observable<void> {
    const body: ResetUserPasswordRequest = {password};
    return this.crudService.put(`${this.base}/${id}/reset-password`, body);
  }

  /** Grants roles. The body is a bare JSON array — `@RequestBody Set<String>`, not an object. */
  assignRoles(id: string, roles: readonly string[]): Observable<void> {
    return this.crudService.post(`${this.base}/${id}/roles`, roles);
  }

  /** Revokes roles. Same bare-array body, on its own path — a POST, not a DELETE. */
  removeRoles(id: string, roles: readonly string[]): Observable<void> {
    return this.crudService.post(`${this.base}/${id}/roles/remove`, roles);
  }

  /**
   * Every role this API may assign.
   *
   * uaa's whole role table minus `USER` and `ORG_ADMIN` — the same list tenancy re-exposes, and it
   * includes `SUPER_ADMIN`. Here that is correct: the caller is one. See lessons.md, "Users —
   * assignable-roles offers SUPER_ADMIN to an org admin", which is about the *other* caller.
   */
  assignableRoles(): Observable<string[]> {
    return this.crudService.get(`${this.base}/assignable-roles`);
  }

  /**
   * One row action, whichever it is.
   *
   * **Why a discriminated union rather than six calls at each call site.** Two screens administer
   * the same accounts — the platform list and an organization's Users tab — and the orchestration
   * around each action (a busy flag, a toast, a re-read) is identical. That orchestration belongs to
   * a facade, and a facade lives in a feature, and a feature may not import another; so without one
   * entry point the six-way branch would be written twice and would drift. This is the part that can
   * be shared, because it is knowledge of the backend and nothing else.
   *
   * `setRoles` is two requests: uaa grants and revokes on separate paths, with no "these are the
   * roles now" endpoint. They run in order and either leg's failure fails the whole action, which is
   * the honest outcome — a half-applied role change is the thing a caller must not be told succeeded.
   * An empty leg is skipped rather than sent as an empty array, and the two answers are collapsed
   * into the single completion the callers subscribe to — `ignoreElements()` would complete without
   * ever emitting, which is a subscriber whose `next` never runs and a toast that never appears.
   */
  apply(id: string, action: AdminUserAction): Observable<void> {
    switch (action.kind) {
      case 'enable':
        return this.enable(id);
      case 'disable':
        return this.disable(id);
      case 'delete':
        return this.delete(id);
      case 'resetPassword':
        return this.resetPassword(id, action.password);
      case 'setRoles':
        return concat(
          action.add.length ? this.assignRoles(id, action.add) : of(undefined),
          action.remove.length ? this.removeRoles(id, action.remove) : of(undefined),
        ).pipe(
          toArray(),
          map(() => undefined),
        );
    }
  }
}

/** What a row action does. See {@link AdminUserService.apply}. */
export type AdminUserAction =
  | {readonly kind: 'enable'}
  | {readonly kind: 'disable'}
  | {readonly kind: 'delete'}
  | {readonly kind: 'resetPassword'; readonly password: string}
  | {readonly kind: 'setRoles'; readonly add: readonly string[]; readonly remove: readonly string[]};
