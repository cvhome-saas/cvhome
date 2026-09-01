import {Component, inject} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {
  BusyOverlay,
  ConfirmDialog,
  EmptyState,
  LoadError,
  NoticeBar,
  PageHeader,
  Pagination,
  Panel,
  RolesDialog,
  SetPasswordDialog,
} from '@cvhome-saas/ui-kit/ui';
import {UserAdminTable, type PlatformUserRow, type UserAdminIntent} from '@cvhome-saas/ui-kit/uaa';

import {PAGE_SIZE, UsersFacade} from './facades/users.facade';

@Component({
  selector: 'app-users',
  imports: [
    TranslocoDirective,
    PageHeader,
    Panel,
    NoticeBar,
    BusyOverlay,
    LoadError,
    EmptyState,
    Pagination,
    UserAdminTable,
    SetPasswordDialog,
    RolesDialog,
    ConfirmDialog,
  ],
  providers: [UsersFacade],
  templateUrl: './users.html',
})
export class Users {
  protected readonly facade = inject(UsersFacade);
  protected readonly pageSize = PAGE_SIZE;

  /** Rendered in a dialog's message, so it needs a name even when the row has none. */
  protected userName(row: PlatformUserRow | null): string {
    return row?.name || row?.username || '';
  }

  /** The table hands roles back as a list; uaa has no display names for them, so they read as-is. */
  protected readonly roleList = (roles: readonly string[]): string => roles.join(', ');

  protected onAction(intent: UserAdminIntent): void {
    switch (intent.kind) {
      case 'toggleEnabled':
        this.facade.toggleEnabled(intent.row);
        break;
      case 'resetPassword':
        this.facade.resetting.set(intent.row);
        break;
      case 'editRoles':
        this.facade.editingRoles.set(intent.row);
        break;
      case 'delete':
        this.facade.deleting.set(intent.row);
        break;
    }
  }
}
