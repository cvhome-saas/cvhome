import {Injectable, inject} from '@angular/core';
import {Observable, forkJoin, map} from 'rxjs';

import {OrgService} from '@api/tenancy/org.service';
import {AdminUserService, type AdminUserAction} from '@api/uaa/admin-user.service';
import {optionalList} from '@core/http/optional';
import {
  toOrgRow,
  toPlatformStoreRow,
  toPlatformUserRow,
  type OrgRow,
  type PlatformStoreRow,
  type PlatformUserRow,
} from '@models/platform';

/** One organization's stores, as the Stores tab reads them. */
export interface OrgStoresSnapshot {
  readonly rows: readonly PlatformStoreRow[];
  readonly totalElements: number;
  readonly totalPages: number;
}

/** One organization's accounts, as the Users tab reads them. */
export interface OrgUsersSnapshot {
  readonly rows: readonly PlatformUserRow[];
  readonly totalElements: number;
  readonly totalPages: number;
  /** What the roles dialog may offer. Optional — an empty list costs the dialog, not the tab. */
  readonly assignableRoles: readonly string[];
}

/**
 * One organization, and the three things that belong to it.
 *
 * Three independent loads rather than one, and the tabs are why: the identity is the page, the
 * stores and the accounts are each a tab's own paged question, and asking all three on arrival would
 * cost two requests nobody looked at.
 *
 * **The accounts come from uaa, not from tenancy.** `OrgMemberApi` is scoped to the *caller's* own
 * organization, so a platform operator cannot use it to read someone else's; uaa's
 * `metadata[org]` filter is org-scoped by construction and is the only join between the two services
 * there is. It means this tab shows uaa's view of a member rather than tenancy's — see lessons.md,
 * "Organizations — another org's members cannot be listed from tenancy".
 */
@Injectable({providedIn: 'root'})
export class OrganizationDetailApi {
  private readonly orgs = inject(OrgService);
  private readonly users = inject(AdminUserService);

  loadOrg(id: string): Observable<OrgRow> {
    return this.orgs.findOne(id).pipe(map(toOrgRow));
  }

  loadStores(id: string, page: number, count: number): Observable<OrgStoresSnapshot> {
    return this.orgs.stores(id, page, count).pipe(
      map((result) => ({
        rows: (result.content ?? []).map(toPlatformStoreRow),
        totalElements: result.totalElements,
        totalPages: result.totalPages,
      })),
    );
  }

  loadUsers(orgId: string, page: number, count: number): Observable<OrgUsersSnapshot> {
    return forkJoin({
      page: this.users.list(page, count, {org: orgId}),
      // Optional: it only fills the roles dialog's checkboxes. Losing the tab to it would be a bad
      // trade, and the dialog says so when the list is empty.
      roles: this.users.assignableRoles().pipe(optionalList()),
    }).pipe(
      map(({page: result, roles}) => ({
        rows: (result.content ?? []).map(toPlatformUserRow),
        totalElements: result.totalElements,
        totalPages: result.totalPages,
        assignableRoles: roles,
      })),
    );
  }

  rename(id: string, name: string): Observable<void> {
    return this.orgs.rename(id, name).pipe(map(() => undefined));
  }

  suspend(id: string, reason: string): Observable<void> {
    return this.orgs.suspend(id, reason).pipe(map(() => undefined));
  }

  resume(id: string): Observable<void> {
    return this.orgs.resume(id).pipe(map(() => undefined));
  }

  close(id: string): Observable<void> {
    return this.orgs.close(id).pipe(map(() => undefined));
  }

  /**
   * Sets the owner's password.
   *
   * The id passed is the **organization's**: tenancy resolves `ownerUserId` from it. Before Module
   * 11's backend change it forwarded that id to uaa unchanged, where a 24-character ObjectId could
   * not bind to a `UUID` path variable — so this endpoint had never once succeeded.
   */
  changeOwnerPassword(id: string, password: string): Observable<void> {
    return this.orgs.changeOwnerPassword(id, password);
  }

  /** One account action, dispatched by the api tier so the two screens that offer them agree. */
  applyToUser(userId: string, action: AdminUserAction): Observable<void> {
    return this.users.apply(userId, action);
  }
}
