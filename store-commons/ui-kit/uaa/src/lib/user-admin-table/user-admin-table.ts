import {Component, computed, inject, input, output} from '@angular/core';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import type {PlatformUserRow} from '../user-row';
import {Badge, DataTable, type TableColumn, TableRow, Icon} from '@cvhome-saas/ui-kit/ui';

/** What a row action asks the host to do. The host owns the dialogs and the writes. */
export interface UserAdminIntent {
  readonly kind: 'toggleEnabled' | 'resetPassword' | 'editRoles' | 'delete';
  readonly row: PlatformUserRow;
}

/** The table's columns. Widths are grid tracks, read straight into the row layout. */
const COLUMN_KEYS: readonly {key: string; labelKey: string; width: string}[] = [
  {key: 'user', labelKey: 'shared.userAdmin.column.user', width: 'minmax(13rem, 2.2fr)'},
  {key: 'roles', labelKey: 'shared.userAdmin.column.roles', width: 'minmax(9rem, 1.3fr)'},
  {key: 'scope', labelKey: 'shared.userAdmin.column.scope', width: 'minmax(9rem, 1.2fr)'},
  {key: 'status', labelKey: 'shared.userAdmin.column.status', width: 'minmax(5rem, 0.6fr)'},
  {key: 'actions', labelKey: '', width: '10rem'},
];

/**
 * Platform accounts, with the operations a super admin has over one.
 *
 * **In `shared/ui/` because two features render it** — the platform-wide account list and an
 * organization's Users tab, which are the same table asking uaa a narrower question. A feature may
 * not import another feature, and this is the part of the two that is identical: rows in, intents
 * out. Everything that is *not* identical — which query produced the rows, which toast a write
 * raises, what a failure means — stays in each feature's facade.
 *
 * It renders no dialog of its own. Confirming a delete and collecting a password are the host's
 * job, so that a page can decide how loudly to ask.
 *
 * `impersonate` is deliberately present and deliberately disabled: the point of writing the
 * requirement is that the screen it belongs to already exists. See
 * `.agents/requirments/user-impersonation.md`, and lessons.md, "Platform — no impersonation".
 */
@Component({
  selector: 'app-user-admin-table',
  imports: [Badge, DataTable, Icon, TableRow, TranslocoDirective],
  templateUrl: './user-admin-table.html',
  styleUrl: './user-admin-table.css',
})
export class UserAdminTable {
  private readonly transloco = inject(TranslocoService);

  readonly rows = input.required<readonly PlatformUserRow[]>();
  readonly label = input.required<string>();
  /** Locks every action while one write is in flight, so two cannot race on the same row. */
  readonly busy = input(false);
  /** Roles in the reader's language. Passed in because the map is a shared service the host holds. */
  readonly roleList = input.required<(roles: readonly string[]) => string>();
  /** How an organization id is rendered: its name where the host knows one, the id where it does not. */
  readonly orgLabel = input<(orgId: string | null) => string>((orgId) => orgId ?? '');
  /** Hides the organization column where every row is in the same organization. */
  readonly showScope = input(true);

  readonly act = output<UserAdminIntent>();

  protected readonly columns = computed<readonly TableColumn[]>(() => {
    this.transloco.activeLang();
    return COLUMN_KEYS.filter((column) => column.key !== 'scope' || this.showScope()).map((column) => ({
      key: column.key,
      label: column.labelKey ? this.transloco.translate(column.labelKey) : '',
      width: column.width,
    }));
  });

  protected emit(kind: UserAdminIntent['kind'], row: PlatformUserRow): void {
    this.act.emit({kind, row});
  }
}
