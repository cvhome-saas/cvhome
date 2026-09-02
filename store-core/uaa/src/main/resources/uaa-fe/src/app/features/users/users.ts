import {Component, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {
  BusyOverlay,
  Checkbox,
  ConfirmDialog,
  EmptyState,
  FormField,
  LoadError,
  NoticeBar,
  PageHeader,
  Pagination,
  Panel,
  SetPasswordDialog,
  TextField,
} from '@cvhome-saas/ui-kit/ui';
import {UserAdminTable, type PlatformUserRow, type UserAdminIntent} from '@cvhome-saas/ui-kit/uaa';

import {PAGE_SIZE, UsersFacade} from './facades/users.facade';

@Component({
  selector: 'app-users',
  imports: [
    ReactiveFormsModule,
    TranslocoDirective,
    PageHeader,
    Panel,
    NoticeBar,
    BusyOverlay,
    LoadError,
    EmptyState,
    Pagination,
    UserAdminTable,
    FormField,
    TextField,
    Checkbox,
    SetPasswordDialog,
    ConfirmDialog,
  ],
  providers: [UsersFacade],
  templateUrl: './users.html',
  styleUrl: './users.css',
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

  /**
   * The table's row actions still exist and still work — they are the fast path.
   *
   * `editRoles` no longer opens a dialog: roles are edited in the pane, so the intent selects the
   * row instead. The two that are genuinely modal keep their dialogs.
   */
  protected onAction(intent: UserAdminIntent): void {
    switch (intent.kind) {
      case 'toggleEnabled':
        this.facade.toggleEnabled(intent.row);
        break;
      case 'resetPassword':
        this.facade.resetting.set(intent.row);
        break;
      case 'editRoles':
        this.facade.select(intent.row);
        break;
      case 'delete':
        this.facade.deleting.set(intent.row);
        break;
    }
  }
}
