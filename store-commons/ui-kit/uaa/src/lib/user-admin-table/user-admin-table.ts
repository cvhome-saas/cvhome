import {Component, computed, inject, input, output} from '@angular/core';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import type {PlatformUserRow} from '../user-row';
import {ActionMenu, Badge, DataTable, type MenuAction, type TableColumn, TableRow} from '@cvhome-saas/ui-kit/ui';

/** What a row action asks the host to do. The host owns the dialogs and the writes. */
export interface UserAdminIntent {
  readonly kind: UserAdminAction;
  readonly row: PlatformUserRow;
}

/** The table's columns. Widths are grid tracks, read straight into the row layout. */
/** What a host may offer on a row. Not every console may offer all of them — see {@link UserAdminTable.allow}. */
export type UserAdminAction = 'toggleEnabled' | 'unlock' | 'resetPassword' | 'editRoles' | 'delete';

const ALL_ACTIONS: readonly UserAdminAction[] = [
  'unlock',
  'toggleEnabled',
  'resetPassword',
  'editRoles',
  'delete',
];

const COLUMN_KEYS: readonly {key: string; labelKey: string; width: string}[] = [
  {key: 'user', labelKey: 'shared.userAdmin.column.user', width: 'minmax(13rem, 2.2fr)'},
  {key: 'roles', labelKey: 'shared.userAdmin.column.roles', width: 'minmax(9rem, 1.3fr)'},
  {key: 'scope', labelKey: 'shared.userAdmin.column.scope', width: 'minmax(9rem, 1.2fr)'},
  {key: 'status', labelKey: 'shared.userAdmin.column.status', width: 'minmax(5rem, 0.6fr)'},
  {key: 'actions', labelKey: 'shared.userAdmin.column.actions', width: '4rem'},
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
 *
 * **The five actions are a menu, not a row of glyphs.** They were five bare `.icon-action` buttons
 * in a 10rem column: an eye, a padlock, a shield, an arrow and a bin, distinguishable only by
 * hovering for a tooltip, and one of them permanently inert with no visible reason. A menu names
 * every one of them in words, gives the disabled entry room to say why it is disabled, and returns
 * the column to the 4rem it actually needs.
 */
@Component({
  selector: 'app-user-admin-table',
  imports: [ActionMenu, Badge, DataTable, TableRow, TranslocoDirective],
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

  /**
   * Which row actions this console may offer. Everything, by default.
   *
   * A merchant administering their own store's shoppers may not set a password or assign a role:
   * those accounts self-register, their roles are the deployment's configuration, and the server
   * exposes no endpoint for either. A menu entry that answers 404 is worse than an absent one, so
   * the host says what it can actually do rather than the table assuming.
   */
  readonly allow = input<readonly UserAdminAction[]>(ALL_ACTIONS);


  /**
   * Turns the rows into a selector.
   *
   * Opt-in, because a row that looks clickable and does nothing is worse than a plain one: the
   * seller console lists accounts and acts on them through the row's own controls, while uaa's
   * console puts the account in a detail pane. Off by default, so the former is unchanged.
   */
  readonly selectable = input(false);
  /** The row currently in the host's detail pane, for the selected state. */
  readonly selectedId = input<string | null>(null);
  readonly picked = output<PlatformUserRow>();

  readonly act = output<UserAdminIntent>();

  protected readonly columns = computed<readonly TableColumn[]>(() => {
    this.transloco.activeLang();
    return COLUMN_KEYS.filter((column) => column.key !== 'scope' || this.showScope()).map((column) => ({
      key: column.key,
      label: column.labelKey ? this.transloco.translate(column.labelKey) : '',
      width: column.width,
    }));
  });

  /**
   * A click on the row selects it — unless it landed inside the actions cell.
   *
   * The whole row is a selector when `selectable`, and the menu sits inside it, so without this a
   * pick would also open the account behind the menu. Read off the event rather than stopped on a
   * wrapper: `stopPropagation` on a `<div>` is a click handler on a non-interactive element, which
   * is a genuine a11y smell and which the template linter is right to refuse.
   */
  protected onRowClick(event: Event, row: PlatformUserRow): void {
    if (!this.selectable()) {
      return;
    }
    if ((event.target as HTMLElement).closest('.cell-actions')) {
      return;
    }
    this.picked.emit(row);
  }

  /**
   * The row's menu, rebuilt per row because two entries read from the row itself.
   *
   * `impersonate` is listed and disabled rather than omitted: a capability the product intends to
   * have is worth showing as not-yet-built, and a menu has room to say so where a bare glyph did
   * not. It carries no `kind`, so picking it cannot reach {@link act}.
   */
  protected actionsFor(row: PlatformUserRow): readonly MenuAction[] {
    const allowed = this.allow();
    return this.everyActionFor(row).filter(
      (action) => action.key === 'impersonate' || allowed.includes(action.key as UserAdminAction),
    );
  }

  private everyActionFor(row: PlatformUserRow): readonly MenuAction[] {
    return [
      ...(row.status === 'LOCKED'
        ? [{key: 'unlock', icon: 'lock' as const, label: this.transloco.translate('shared.userAdmin.action.unlock')}]
        : []),
      {
        key: 'toggleEnabled',
        icon: row.enabled ? 'eyeOff' : 'eye',
        label: row.enabled
          ? this.transloco.translate('shared.userAdmin.action.disable')
          : this.transloco.translate('shared.userAdmin.action.enable'),
      },
      {
        key: 'resetPassword',
        icon: 'lock',
        label: this.transloco.translate('shared.userAdmin.action.resetPassword'),
      },
      {
        key: 'editRoles',
        icon: 'shield',
        label: this.transloco.translate('shared.userAdmin.action.editRoles'),
      },
      {
        key: 'impersonate',
        icon: 'signIn',
        disabled: true,
        label: this.transloco.translate('shared.userAdmin.action.impersonateUnavailable'),
      },
      {
        key: 'delete',
        icon: 'trash',
        danger: true,
        label: this.transloco.translate('shared.userAdmin.action.delete'),
      },
    ];
  }

  /** The status badge's tone: the one word uaa derives for the account. */
  protected statusTone(row: PlatformUserRow): 'green' | 'amber' | 'red' | 'slate' {
    switch (row.status) {
      case 'ACTIVE':
        return 'green';
      case 'PENDING':
        return 'amber';
      case 'LOCKED':
        return 'red';
      default:
        return 'slate';
    }
  }

  /** Only the real intents reach the host; `impersonate` is inert by design. */
  protected onPick(action: MenuAction, row: PlatformUserRow): void {
    if (action.key === 'impersonate') {
      return;
    }
    this.act.emit({kind: action.key as UserAdminIntent['kind'], row});
  }
}
