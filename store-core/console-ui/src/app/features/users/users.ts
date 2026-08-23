import {Component, ElementRef, computed, effect, inject, input, viewChild} from '@angular/core';

import type {TeamRow} from '@models/team';
import {Router} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {TranslocoDatePipe} from '@jsverse/transloco-locale';

import {Badge} from '@shared/ui/badge/badge';
import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {ConfirmDialog} from '@shared/ui/confirm-dialog/confirm-dialog';
import {DataTable, type TableColumn} from '@shared/ui/data-table/data-table';
import {TableRow} from '@shared/ui/data-table/table-row';
import {EmptyState} from '@shared/ui/empty-state/empty-state';
import {ExportButton} from '@shared/ui/export-button/export-button';
import {Icon} from '@shared/ui/icon/icon';
import {LoadError} from '@shared/ui/load-error/load-error';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {Pagination} from '@shared/ui/pagination/pagination';
import {Panel} from '@shared/ui/panel/panel';
import {Select, type SelectOption} from '@shared/ui/select/select';
import {TabSwitcher} from '@shared/ui/tab-switcher/tab-switcher';
import {InvitationLinkDialog} from './components/invitation-link-dialog/invitation-link-dialog';
import {InviteDialog} from './components/invite-dialog/invite-dialog';
import {UserDialog} from './components/user-dialog/user-dialog';
import {SetPasswordDialog} from '@shared/ui/set-password-dialog/set-password-dialog';
import {PAGE_SIZE, UsersFacade} from './facades/users.facade';

/** The team table's columns. Widths are grid tracks, read straight into the row layout. */
const COLUMN_KEYS: readonly {key: string; labelKey: string; width: string; align?: 'start' | 'end'}[] = [
  {key: 'user', labelKey: 'users.column.user', width: 'minmax(14rem, 2.2fr)'},
  {key: 'username', labelKey: 'users.column.userName', width: 'minmax(9rem, 1.2fr)'},
  {key: 'roles', labelKey: 'users.column.roles', width: 'minmax(9rem, 1.3fr)'},
  {key: 'status', labelKey: 'users.column.status', width: 'minmax(6rem, 0.7fr)'},
  {key: 'actions', labelKey: '', width: '3.5rem'},
];

/** The invitations table's columns. */
const INVITATION_COLUMN_KEYS: readonly {key: string; labelKey: string; width: string}[] = [
  {key: 'email', labelKey: 'users.column.email', width: 'minmax(11rem, 2fr)'},
  {key: 'role', labelKey: 'users.column.role', width: 'minmax(8rem, 1fr)'},
  {key: 'status', labelKey: 'users.column.status', width: 'minmax(6rem, 0.8fr)'},
  {key: 'expires', labelKey: 'users.column.expires', width: 'minmax(7rem, 1fr)'},
  {key: 'actions', labelKey: '', width: '5rem'},
];

/**
 * User management — the team, and who has been asked to join it.
 *
 * Everyone with access to the open store, and what may be done to their account. The design merges
 * staff and customers into one table; they come from two services with no key in common and the
 * customer half is its own module, so this page is the team.
 *
 * What the design has and this does not: an avatar photo, a phone number, a postal address, a "last
 * active" column, a lifetime value, an order tally per person and a running count of admin actions.
 * A uaa user is eight columns and none of those is one of them — see lessons.md, the Users entries.
 * The search box goes the same way: uaa's list matches on metadata equality and offers no name,
 * email or username query at all.
 */
@Component({
  selector: 'app-users',
  imports: [
    Badge,
    BusyOverlay,
    ConfirmDialog,
    DataTable,
    EmptyState,
    ExportButton,
    Icon,
    InvitationLinkDialog,
    InviteDialog,
    LoadError,
    PageHeader,
    Pagination,
    Panel,
    Select,
    SetPasswordDialog,
    TabSwitcher,
    TableRow,
    TranslocoDatePipe,
    TranslocoDirective,
    UserDialog,
  ],
  providers: [UsersFacade],
  templateUrl: './users.html',
  /*
   * `field.css` carries `.page-body`, `.split` and `.field-hint`, and every feature that uses them
   * pulls it in beside its own sheet — it is not global. Omitting it left `.split` as a plain block,
   * so the master-detail panes stacked no matter how wide the page was. Found in QA.
   */
  styleUrl: './users.css',
})
export class Users {
  private readonly transloco = inject(TranslocoService);
  private readonly router = inject(Router);

  protected readonly facade = inject(UsersFacade);

  /**
   * The selected user, from `?user=`, bound by `withComponentInputBinding()`.
   *
   * In the URL rather than in a signal alone so that a rail an operator has open survives a reload
   * and can be linked to — the page contract, applied to a master-detail page.
   */
  readonly user = input<string>();

  /**
   * The active tab, from `:tab`, bound by `withComponentInputBinding()`.
   *
   * An unknown value falls back to `team` in the effect below rather than being matched in the route
   * — a fixed list there would make adding a tab a two-file change, which is the call the catalogue
   * made for the same reason.
   */
  readonly tab = input<string>();

  protected readonly isLoading = this.facade.isLoading;
  protected readonly isEmpty = this.facade.isEmpty;
  protected readonly error = this.facade.error;
  protected readonly rows = this.facade.rows;
  protected readonly heading = this.facade.heading;
  protected readonly busy = this.facade.busy;
  protected readonly pageSize = PAGE_SIZE;

  /** The region the export captures. Absent until the first response renders it. */
  protected readonly report = viewChild('report', {read: ElementRef});

  /*
   * Bound once as fields rather than passed as `facade.x.bind(facade)` in the template: a method
   * reference created in a binding is a new function every change detection, which makes the
   * dialog's inputs look changed on every tick.
   */
  protected readonly roleLabel = (role: string) => this.facade.roleLabel(role);
  protected readonly roleList = (roles: readonly string[]) => this.facade.roleList(roles);
  protected readonly initials = (row: TeamRow) => this.facade.initialsOf(row);
  protected readonly hasRole = (role: string) => this.facade.hasRole(role);

  protected readonly columns = computed<readonly TableColumn[]>(() => {
    this.transloco.activeLang();
    return COLUMN_KEYS.map((column) => ({
      key: column.key,
      label: column.labelKey ? this.transloco.translate(column.labelKey) : '',
      width: column.width,
      align: column.align,
    }));
  });

  protected readonly invitationColumns = computed<readonly TableColumn[]>(() => {
    this.transloco.activeLang();
    return INVITATION_COLUMN_KEYS.map((column) => ({
      key: column.key,
      label: column.labelKey ? this.transloco.translate(column.labelKey) : '',
      width: column.width,
    }));
  });

  /**
   * The roles the invite dialog offers.
   *
   * The same narrowed set the create form uses, so an invitation cannot grant a role a directly
   * created account could not. `STORE_ADMIN` leads because it is the endpoint's own default, which
   * makes the pre-selection match what omitting the parameter would do.
   */
  protected readonly roleOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return ['STORE_ADMIN', 'STORE_MODERATOR']
      .filter((role) => this.facade.assignableRoles().includes(role))
      .map((role) => ({value: role, label: this.facade.roleLabel(role)}));
  });

  constructor() {
    /*
     * The URL is what a reload and a shared link restore the rail from. It is not the only writer —
     * `select` sets the facade directly and mirrors here — so this only has to carry the cases the
     * page did not cause: a first render, a back button, a pasted link.
     */
    /*
     * The route segment is what a reload and a shared link restore the tab from — `onTab` is the
     * other writer. An unknown segment settles on `team` here rather than being matched in the
     * route, so adding a tab stays a one-file change.
     */
    effect(() => {
      const requested = this.tab();
      if (requested) {
        this.facade.activeTab.set(requested === 'invitations' ? 'invitations' : 'team');
      }
    });

    effect(() => {
      const fromUrl = this.user();
      if (fromUrl && fromUrl !== this.facade.selectedId()) {
        this.facade.selectRow(fromUrl);
      }
    });
  }

  /**
   * Selecting sets the rail and mirrors the choice into the URL.
   *
   * The state leads and the URL follows, rather than the other way round: `Router.navigate` has not
   * resolved by the time the next statement runs, so a rail that waited for the URL to come back
   * around would lag a click behind — and would not open at all anywhere the navigation does not
   * resolve. The effect above then re-sets the same value, which is a no-op.
   */
  protected open(id: string): void {
    this.facade.selectRow(id);
    void this.router.navigate([], {queryParams: {user: id}, queryParamsHandling: 'merge'});
  }

  /** Leaving the form returns to reading the same account, or closes when there was none. */
  protected onCancelEdit(): void {
    this.facade.cancelEdit();
    if (!this.facade.selectedId()) {
      this.closeDialog();
    }
  }

  protected closeDialog(): void {
    this.facade.clearSelection();
    void this.router.navigate([], {queryParams: {user: null}, queryParamsHandling: 'merge'});
  }

  /**
   * The tab is a route segment, not a query parameter.
   *
   * `?user=` is dropped on the way: a selection made on the team tab means nothing on the
   * invitations one, and carrying it would restore a rail the operator cannot see.
   */
  protected onTab(tab: string): void {
    // The state leads and the URL mirrors it, as with the selection: a tab that waited for the
    // navigation to resolve would lag a click behind.
    this.facade.activeTab.set(tab === 'invitations' ? 'invitations' : 'team');
    void this.router.navigate(['/users', tab]);
  }

  protected onPage(page: number): void {
    this.facade.goToPage(page);
  }
}
