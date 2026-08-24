import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {SpringPage} from '@core/table/table.types';
import type {ManagerOrgDto, ManagerStore} from '@models/tenancy';
import type {ReadableUser} from '@models/users';

/**
 * The tenant registry: every organization on the platform, and the levers over one.
 *
 * **Super-admin only, on every method** — `OrgManagerApi` carries
 * `hasAnyRole('ROLE_SUPER_ADMIN')` class-wide-by-repetition. That is also why an org admin cannot
 * read its own organization anywhere in this console; see lessons.md, "Shell — an org admin cannot
 * read its own organization".
 *
 * **Three of seller-core's five methods are not ported, and one is corrected.**
 *
 * - `updateOrg()` → `PUT org-manager/update`. **No controller maps that path.** The operation that
 *   exists is `POST rename?id=&name=`, which is {@link rename} below.
 * - `getSubscriptionPlans()` → billing's public plan catalog. Its own doc comment says a plan belongs
 *   to a store now, so the create-org and update-org screens that offered it were choosing something
 *   applied nowhere. `CreateOrgRequest` is `(PersistableUser user)` and has no plan field at all.
 * - `changeOrgPassword()` sent `{password}`. `UserPassword.getChangePassword()` is the field the
 *   server reads, so seller-ui's reset would have sent null even if it had reached the right user.
 *   See {@link changeOwnerPassword}.
 *
 * **Four lifecycle methods are new here** — `rename`, `suspend`, `resume` and `close` exist on the
 * server, are audited into `tenancy_audit`, and had no caller in any frontend.
 */
export const ORG_MANAGER_API_BASE = '/tenancy/api/v1/org-manager';

@Injectable({providedIn: 'root'})
export class OrgService {
  private readonly crudService = inject(CrudService);

  /**
   * A page of every organization on the platform.
   *
   * `count`, not Spring's `size`: `store-commons:autoconfigure`'s `ServletWebConfig` renames the
   * page-size parameter platform-wide.
   *
   * Unfiltered, and that is all this one does — `findAllOrg` takes a bare `Pageable`. {@link search}
   * is what the tenant registry screen binds to; this stays because two callers want every
   * organization and no filter: the pod fleet's owner lookup and the pod form's owner picker.
   */
  list(page: number, count: number): Observable<SpringPage<ManagerOrgDto>> {
    return this.crudService.get(`${ORG_MANAGER_API_BASE}/find-all`, {page, count});
  }

  /**
   * The same page, narrowed by a search term and a status.
   *
   * A POST carrying a query body, the same shape `store-manager/list` uses — the two are the same table twice
   * over. `find-all` above stays for the callers that want no filter at all, such as the pod screens' owner
   * lookup.
   *
   * **The term spans the name and the contact email**, server-side and case-insensitively. One box against one
   * parameter, because almost every organization on the platform is unnamed and is therefore *listed* by its
   * email — a box that searched only the name would fail to find the rows on screen.
   */
  search(query: OrgQuery, page: number, count: number): Observable<SpringPage<ManagerOrgDto>> {
    const term = query.term.trim();
    return this.crudService.post(
      `${ORG_MANAGER_API_BASE}/list`,
      {term: term || null, status: query.status || null},
      {page, count},
    );
  }

  /**
   * One organization by id.
   *
   * The id is a 24-character `ManagerOrgId`, passed as a bare string — Spring converts it through
   * the record's `(String)` constructor, which yields a null inner id for anything that is not 24
   * characters rather than failing to bind.
   */
  findOne(id: string): Observable<ManagerOrgDto> {
    return this.crudService.get(`${ORG_MANAGER_API_BASE}/find-one`, {id});
  }

  /**
   * Creates an organization and its first administrator, or neither.
   *
   * `SignupServiceImpl.createOrgUser` holds both in one transaction and forces `active`, the
   * username (which is the email), the org id and the `ORG_ADMIN` role itself, so those are not sent
   * — a value the server overwrites is a value that should not be in the payload.
   *
   * The organization cannot be *named* here: `ManagerOrgEntity.createOrgFromUser` sets id, created
   * date, email and status and nothing else. A name is set afterwards, through {@link rename}. See
   * lessons.md, "Organizations — an org cannot be named at creation".
   */
  create(user: CreateOrgUser): Observable<ReadableUser> {
    return this.crudService.post(`${ORG_MANAGER_API_BASE}/create`, {user});
  }

  /**
   * Sets the organization owner's password.
   *
   * **`changePassword`, not `password`.** `UserPassword` has both fields and
   * `UserAccountServiceImpl.changePassword` forwards only `getChangePassword()`; seller-ui sent the
   * other one.
   *
   * The `id` is the **organization's**, not the owner's: the server resolves
   * `ManagerOrgDto.ownerUserId` from it. Before Module 11's backend change it passed the org id
   * straight to uaa, where `resetPassword` declares `@PathVariable UUID id` and a 24-character
   * ObjectId cannot bind — so this endpoint had never once succeeded.
   */
  changeOwnerPassword(id: string, password: string): Observable<void> {
    return this.crudService.post(`${ORG_MANAGER_API_BASE}/change-password`, {changePassword: password}, {id});
  }

  /**
   * A page of one organization's stores.
   *
   * Read-only on this side of the console: a store is administered by its own organization, and
   * nothing on `OrgManagerApi` writes one.
   */
  stores(id: string, page: number, count: number): Observable<SpringPage<ManagerStore>> {
    return this.crudService.get(`${ORG_MANAGER_API_BASE}/stores`, {id, page, count});
  }

  /** Names an organization, and records who did it in `tenancy_audit`. Query parameters, not a body. */
  rename(id: string, name: string): Observable<ManagerOrgDto> {
    return this.crudService.post(`${ORG_MANAGER_API_BASE}/rename`, null, {id, name});
  }

  /**
   * Suspends an organization, and with it every store it owns.
   *
   * No store row is written. `InternalStoreService.requireOperable` reads the org's status as well as
   * the store's, so suspension takes effect everywhere at once rather than fanning out writes that
   * drift when one of them fails.
   *
   * `reason` is optional on the server and defaults to `"suspended by operator"`; it is sent only
   * when the operator typed one, so the audit row carries their words rather than an empty string.
   */
  suspend(id: string, reason?: string): Observable<ManagerOrgDto> {
    return this.crudService.post(`${ORG_MANAGER_API_BASE}/suspend`, null, {id, reason: reason || undefined});
  }

  /** Returns a suspended organization to service. */
  resume(id: string): Observable<ManagerOrgDto> {
    return this.crudService.post(`${ORG_MANAGER_API_BASE}/resume`, null, {id});
  }

  /**
   * Closes an organization. Terminal: `OrgLifecycleService` allows no transition out of `CLOSED`.
   *
   * An illegal transition comes back as `IllegalLifecycleTransitionException`, which the console
   * renders as the server's refusal rather than predicting.
   */
  close(id: string): Observable<ManagerOrgDto> {
    return this.crudService.post(`${ORG_MANAGER_API_BASE}/close`, null, {id});
  }
}

/**
 * The first administrator, as `CreateOrgRequest`'s `PersistableUser` half.
 *
 * Narrower than `@models/users`'s `PersistableUser` on purpose: `SignupServiceImpl` sets `userName`,
 * `active`, `org` and `roles` itself, so those four are not the caller's to send.
 */
/** What the organizations list filters on. Both are optional and the server ANDs them. */
export interface OrgQuery {
  /** Matched against the name and the contact email. */
  readonly term: string;
  /** An `OrgStatus`, or `''` for any. */
  readonly status: string;
}

export interface CreateOrgUser {
  readonly firstName: string;
  readonly lastName: string;
  readonly emailAddress: string;
  readonly password: string;
}
