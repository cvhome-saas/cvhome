import {Component, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {
  Badge,
  BusyOverlay,
  Checkbox,
  ConfirmDialog,
  DataTable,
  EmptyState,
  FormDialog,
  FormField,
  KpiGrid,
  LoadError,
  OneTimeLinkDialog,
  PageHeader,
  Pagination,
  Panel,
  SearchBox,
  SetPasswordDialog,
  TabSwitcher,
  TableRow,
  TextField,
  Toggle,
  type TableColumn,
} from '@cvhome-saas/ui-kit/ui';
import {UserAdminTable, type InvitationDto, type UserAdminIntent} from '@cvhome-saas/ui-kit/uaa';

import {PairList} from '@shared/ui/pair-list/pair-list';

import {PAGE_SIZE, UsersFacade} from './facades/users.facade';

@Component({
  selector: 'app-users',
  imports: [
    ReactiveFormsModule,
    TranslocoDirective,
    PageHeader,
    Panel,
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
    OneTimeLinkDialog,
    Badge,
    KpiGrid,
    TabSwitcher,
    SearchBox,
    DataTable,
    TableRow,
  ],
  providers: [UsersFacade],
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class Users {
  protected readonly facade = inject(UsersFacade);
  protected readonly pageSize = PAGE_SIZE;

  /** The invitations table: who, state, when it runs out, who issued it, and the two actions. */
  private readonly invitationKeys: readonly {key: string; width: string}[] = [
    {key: 'who', width: 'minmax(14rem, 2fr)'},
    {key: 'status', width: 'minmax(6rem, 0.7fr)'},
    {key: 'expires', width: 'minmax(9rem, 1fr)'},
    {key: 'createdBy', width: 'minmax(8rem, 1fr)'},
    {key: 'actions', width: 'minmax(9rem, 0.9fr)'},
  ];

  protected invitationColumns(t: (key: string) => string): readonly TableColumn[] {
    return this.invitationKeys.map(({key, width}) => ({key, width, label: t('users.invitations.column.' + key)}));
  }

  /** A timestamp for the security section, in the reader's locale; `—` when uaa has none. */
  protected when(value: string | null): string {
    return value ? new Date(value).toLocaleString() : '—';
  }

  protected invitationTone(invitation: InvitationDto): 'green' | 'amber' | 'red' | 'slate' {
    switch (invitation.status) {
      case 'ACCEPTED':
        return 'green';
      case 'PENDING':
        return 'amber';
      case 'REVOKED':
        return 'red';
      default:
        return 'slate';
    }
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
