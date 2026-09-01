import {Injectable, inject} from '@angular/core';
import {Observable, forkJoin, map} from 'rxjs';

import {OrgService} from '@api/tenancy/org.service';
import {AdminUserService, type AdminUserAction} from '@cvhome-saas/ui-kit/uaa';
import {optionalOne} from '@cvhome-saas/ui-kit';
import {toOrgRow, toPlatformUserRow, type PlatformUserRow} from '@models/platform';

/** How many organizations the filter offers. See `pods.api.service.ts` for why there is a cap. */
export const ORG_FILTER_LIMIT = 200;

/** One organization, as the filter lists it. */
export interface OrgChoice {
  readonly id: string;
  readonly label: string;
}

/** Everything the account list renders for one query. */
export interface PlatformUsersSnapshot {
  readonly rows: readonly PlatformUserRow[];
  readonly totalElements: number;
  readonly totalPages: number;
  readonly orgs: readonly OrgChoice[];
  readonly orgNames: ReadonlyMap<string, string>;
  readonly assignableRoles: readonly string[];
}

/** What the page asks for: a page, and the one filter uaa understands. */
export interface PlatformUsersQuery {
  readonly page: number;
  readonly count: number;
  /** An organization id, or `''` for every account on the platform. */
  readonly org: string;
}

/**
 * Every account on the platform.
 *
 * **The filter is metadata equality and nothing else.** `AdminService.getUsers` reads
 * `metadata[<key>]` query parameters and matches on equality; there is no query over username, email
 * or name at all, which is why this page offers an organization picker and no search box. See
 * lessons.md, "Platform users — no text search".
 *
 * Two optional legs ride along: the organization list, which fills the filter and names the scope
 * column, and the assignable roles, which fill the roles dialog. Neither is the page.
 */
@Injectable({providedIn: 'root'})
export class PlatformUsersApi {
  private readonly users = inject(AdminUserService);
  private readonly orgs = inject(OrgService);

  load(query: PlatformUsersQuery): Observable<PlatformUsersSnapshot> {
    return forkJoin({
      page: this.users.list(query.page, query.count, query.org ? {org: query.org} : {}),
      // Optional: it fills the filter and names the scope column. An id is still a usable column.
      orgs: this.orgs.list(0, ORG_FILTER_LIMIT).pipe(optionalOne()),
      // Optional: it fills the roles dialog, which says so when the list is empty.
      roles: this.users.assignableRoles().pipe(optionalOne()),
    }).pipe(
      map(({page, orgs, roles}) => {
        const choices = (orgs?.content ?? []).map(toOrgRow).map((org) => ({id: org.id, label: org.label}));
        return {
          rows: (page.content ?? []).map(toPlatformUserRow),
          totalElements: page.totalElements,
          totalPages: page.totalPages,
          orgs: choices,
          orgNames: new Map(choices.map((org) => [org.id, org.label] as const)),
          assignableRoles: roles ?? [],
        };
      }),
    );
  }

  /** One row action, dispatched by the api tier so the two screens that offer them agree. */
  apply(userId: string, action: AdminUserAction): Observable<void> {
    return this.users.apply(userId, action);
  }
}
