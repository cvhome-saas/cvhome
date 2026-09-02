import {Component, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {
  BusyOverlay,
  Checkbox,
  ConfirmDialog,
  EmptyState,
  FormDialog,
  FormField,
  LoadError,
  NoticeBar,
  PageHeader,
  Pagination,
  Panel,
  Badge,
  SetPasswordDialog,
  TextField,
  Toggle,
} from '@cvhome-saas/ui-kit/ui';
import {UserAdminTable, type UserAdminIntent} from '@cvhome-saas/ui-kit/uaa';

import {PairList} from '@shared/ui/pair-list/pair-list';

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
    FormDialog,
    FormField,
    TextField,
    Checkbox,
    Toggle,
    PairList,
    SetPasswordDialog,
    ConfirmDialog,
    Badge,
  ],
  providers: [UsersFacade],
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class Users {
  protected readonly facade = inject(UsersFacade);
  protected readonly pageSize = PAGE_SIZE;

  /** A timestamp for the security section, in the reader's locale; `—` when uaa has none. */
  protected when(value: string | null): string {
    return value ? new Date(value).toLocaleString() : '—';
  }

  /** The table hands roles back as a list; uaa has no display names for them, so they read as-is. */
  protected readonly roleList = (roles: readonly string[]): string => roles.join(', ');

  /**
   * The table's row actions still exist and still work — they are the fast path.
   *
   * `editRoles` opens the same dialog the row itself does: roles are one field of the account, not
   * a screen of their own. The two that are genuinely modal keep their own dialogs.
   */
  protected onAction(intent: UserAdminIntent): void {
    switch (intent.kind) {
      case 'toggleEnabled':
        this.facade.toggleEnabled(intent.row);
        break;
      case 'unlock':
        this.facade.unlockRow(intent.row);
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
