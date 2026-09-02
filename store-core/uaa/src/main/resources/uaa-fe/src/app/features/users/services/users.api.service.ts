import {Injectable, inject} from '@angular/core';
import {Observable, map} from 'rxjs';

import {AdminUserService, toPlatformUserRow, type PlatformUserRow} from '@cvhome-saas/ui-kit/uaa';

export interface UserPage {
  readonly rows: readonly PlatformUserRow[];
  readonly totalElements: number;
  readonly totalPages: number;
}

/**
 * The seam onto `@cvhome-saas/ui-kit/uaa`, where a `UserDto` becomes a row.
 *
 * The same shape console-ui's `PlatformUsersApi` has, and for the same reason: keeping the mapping
 * here means the facade, the page and their specs do not move when an endpoint does. The two are not
 * shared because they answer different questions — this console lists everyone unconditionally,
 * while the platform console filters by organization.
 */
@Injectable({providedIn: 'root'})
export class UsersApi {
  private readonly admin = inject(AdminUserService);

  load(query: {page: number; count: number}): Observable<UserPage> {
    return this.admin.list(query.page, query.count).pipe(
      map((page) => ({
        rows: page.content.map(toPlatformUserRow),
        totalElements: page.totalElements,
        totalPages: page.totalPages,
      })),
    );
  }

  assignableRoles(): Observable<readonly string[]> {
    return this.admin.assignableRoles();
  }
}
