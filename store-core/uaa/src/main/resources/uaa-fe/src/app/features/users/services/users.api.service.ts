import {Injectable, inject} from '@angular/core';
import {Observable, map} from 'rxjs';

import type {SpringPage} from '@cvhome-saas/ui-kit';
import {
  AdminUserService,
  toPlatformUserRow,
  type InvitationDto,
  type InvitationStatus,
  type PlatformUserRow,
  type UserCounts,
  type UserStatus,
} from '@cvhome-saas/ui-kit/uaa';

export interface UserPage {
  readonly rows: readonly PlatformUserRow[];
  readonly totalElements: number;
  readonly totalPages: number;
}

export interface UserQuery {
  readonly page: number;
  readonly count: number;
  readonly q: string;
  readonly status: UserStatus | '';
}

/**
 * The seam onto `@cvhome-saas/ui-kit/uaa`, where a `UserDto` becomes a row.
 *
 * The same shape console-ui's `PlatformUsersApi` has, and for the same reason: keeping the mapping
 * here means the facade, the page and their specs do not move when an endpoint does. The two are not
 * shared because they answer different questions — this console lists everyone, filtered by text
 * and status, while the platform console filters by organization.
 */
@Injectable({providedIn: 'root'})
export class UsersApi {
  private readonly admin = inject(AdminUserService);

  load(query: UserQuery): Observable<UserPage> {
    return this.admin.search(query.page, query.count, {q: query.q, status: query.status}).pipe(
      map((page) => ({
        rows: page.content.map(toPlatformUserRow),
        totalElements: page.totalElements,
        totalPages: page.totalPages,
      })),
    );
  }

  counts(): Observable<UserCounts> {
    return this.admin.counts();
  }

  invitations(page: number, count: number, status: InvitationStatus | ''): Observable<SpringPage<InvitationDto>> {
    return this.admin.invitations(page, count, status);
  }

  assignableRoles(): Observable<readonly string[]> {
    return this.admin.assignableRoles();
  }
}
