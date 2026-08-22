import {Component, ElementRef, computed, effect, inject, input, viewChild} from '@angular/core';
import {Router} from '@angular/router';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {Badge} from '@shared/ui/badge/badge';
import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {Checkbox} from '@shared/ui/checkbox/checkbox';
import {ConfirmDialog} from '@shared/ui/confirm-dialog/confirm-dialog';
import {DataTable, type TableColumn} from '@shared/ui/data-table/data-table';
import {TableRow} from '@shared/ui/data-table/table-row';
import {EmptyState} from '@shared/ui/empty-state/empty-state';
import {ExportButton} from '@shared/ui/export-button/export-button';
import {FieldError} from '@shared/ui/form-field/field-error';
import {FormField} from '@shared/ui/form-field/form-field';
import {Icon} from '@shared/ui/icon/icon';
import {KpiGrid} from '@shared/ui/kpi-grid/kpi-grid';
import {LoadError} from '@shared/ui/load-error/load-error';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {Pagination} from '@shared/ui/pagination/pagination';
import {Panel} from '@shared/ui/panel/panel';
import {TextField} from '@shared/ui/text-field/text-field';
import {Toggle} from '@shared/ui/toggle/toggle';
import {SetPasswordDialog} from './components/set-password-dialog/set-password-dialog';
import {PAGE_SIZE, UsersFacade} from './facades/users.facade';

/** The team table's columns. Widths are grid tracks, read straight into the row layout. */
const COLUMN_KEYS: readonly {key: string; labelKey: string; width: string; align?: 'start' | 'end'}[] = [
  {key: 'user', labelKey: 'users.column.user', width: 'minmax(11rem, 2fr)'},
  {key: 'roles', labelKey: 'users.column.roles', width: 'minmax(8rem, 1.2fr)'},
  {key: 'status', labelKey: 'users.column.status', width: 'minmax(6rem, 0.8fr)'},
  {key: 'actions', labelKey: '', width: '5rem'},
];

/**
 * User management — the team half.
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
    Checkbox,
    ConfirmDialog,
    DataTable,
    EmptyState,
    ExportButton,
    FieldError,
    FormField,
    Icon,
    KpiGrid,
    LoadError,
    NoticeBar,
    PageHeader,
    Pagination,
    Panel,
    ReactiveFormsModule,
    SetPasswordDialog,
    TableRow,
    TextField,
    Toggle,
    TranslocoDirective,
  ],
  providers: [UsersFacade],
  templateUrl: './users.html',
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

  protected readonly isLoading = this.facade.isLoading;
  protected readonly isEmpty = this.facade.isEmpty;
  protected readonly error = this.facade.error;
  protected readonly rows = this.facade.rows;
  protected readonly kpis = this.facade.kpis;
  protected readonly heading = this.facade.heading;
  protected readonly busy = this.facade.busy;
  protected readonly pageSize = PAGE_SIZE;

  /** The region the export captures. Absent until the first response renders it. */
  protected readonly report = viewChild('report', {read: ElementRef});

  protected readonly columns = computed<readonly TableColumn[]>(() => {
    this.transloco.activeLang();
    return COLUMN_KEYS.map((column) => ({
      key: column.key,
      label: column.labelKey ? this.transloco.translate(column.labelKey) : '',
      width: column.width,
      align: column.align,
    }));
  });

  constructor() {
    /*
     * The URL is what a reload and a shared link restore the rail from. It is not the only writer —
     * `select` sets the facade directly and mirrors here — so this only has to carry the cases the
     * page did not cause: a first render, a back button, a pasted link.
     */
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
  protected select(id: string): void {
    this.facade.selectRow(id);
    void this.router.navigate([], {queryParams: {user: id}, queryParamsHandling: 'merge'});
  }

  /**
   * Selecting and editing in one gesture, for the row's pencil.
   *
   * Sets the facade's selection before navigating rather than waiting for the URL to come back
   * around: `startEdit` refuses when nothing is selected, and a `Router.navigate` has not resolved
   * by the time the next statement runs. The effect above then re-sets the same value, which is a
   * no-op.
   */
  protected edit(row: {id: string}): void {
    this.facade.selectRow(row.id);
    this.facade.startEdit();
    void this.router.navigate([], {queryParams: {user: row.id}, queryParamsHandling: 'merge'});
  }

  /** Leaving the form returns to reading the same user, or to nothing when there was none. */
  protected cancelEdit(): void {
    this.facade.cancelEdit();
    if (!this.facade.selectedId()) {
      this.closeRail();
    }
  }

  protected closeRail(): void {
    this.facade.clearSelection();
    void this.router.navigate([], {queryParams: {user: null}, queryParamsHandling: 'merge'});
  }

  protected onPage(page: number): void {
    this.facade.goToPage(page);
  }
}
