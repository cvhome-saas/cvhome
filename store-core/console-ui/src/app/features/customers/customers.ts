import {Component, ElementRef, computed, effect, inject, input, linkedSignal, untracked, viewChild} from '@angular/core';
import {Router} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import type {CustomerOrderRow} from '@models/customers';
import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {DataTable, type TableColumn} from '@shared/ui/data-table/data-table';
import {TableRow} from '@shared/ui/data-table/table-row';
import {EmptyState} from '@shared/ui/empty-state/empty-state';
import {ExportButton} from '@shared/ui/export-button/export-button';
import {Icon} from '@shared/ui/icon/icon';
import {LoadError} from '@shared/ui/load-error/load-error';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {Pagination} from '@shared/ui/pagination/pagination';
import {Panel} from '@shared/ui/panel/panel';
import {SearchBox} from '@shared/ui/search-box/search-box';
import {CustomerDialog} from './components/customer-dialog/customer-dialog';
import {CustomersFacade, PAGE_SIZE} from './facades/customers.facade';

/** The table's columns. Widths are grid tracks, read straight into the row layout. */
const COLUMN_KEYS: readonly {key: string; labelKey: string; width: string}[] = [
  {key: 'customer', labelKey: 'customers.column.customer', width: 'minmax(14rem, 2.4fr)'},
  {key: 'location', labelKey: 'customers.column.location', width: 'minmax(9rem, 1.2fr)'},
  {key: 'id', labelKey: 'customers.column.id', width: 'minmax(5rem, 0.6fr)'},
  {key: 'actions', labelKey: '', width: '3.5rem'},
];

/**
 * The people who have bought from the open store.
 *
 * **The thinnest record the console renders, and honestly so.** `ReadableCustomer` is eight fields,
 * two of which are addresses. There is no created-at, no account status, no group, no tag, no note,
 * no spend and no order tally on it — so the design's KPI row, its Active/Invited/Suspended badge,
 * its per-row completed and open order columns and its multi-address book are all absent rather
 * than filled in. What is here is what a customer record holds, plus the one join that is real:
 * their orders, counted exactly.
 *
 * The search box works because this module bound the filters `CustomerRepository` had implemented
 * and `CustomerApi.list` never populated — seller-ui's list could not search at all.
 */
@Component({
  selector: 'app-customers',
  imports: [
    BusyOverlay,
    CustomerDialog,
    DataTable,
    EmptyState,
    ExportButton,
    Icon,
    LoadError,
    PageHeader,
    Pagination,
    Panel,
    SearchBox,
    TableRow,
    TranslocoDirective,
  ],
  providers: [CustomersFacade],
  templateUrl: './customers.html',
  styleUrl: './customers.css',
})
export class Customers {
  private readonly transloco = inject(TranslocoService);
  private readonly router = inject(Router);

  protected readonly facade = inject(CustomersFacade);

  /**
   * The open customer, from `?customer=`, bound by `withComponentInputBinding()`.
   *
   * In the URL rather than in a signal alone so that a record an operator has open survives a
   * reload and can be linked to — the page contract.
   */
  readonly customer = input<string>();

  /**
   * The search term, from `?q=`.
   *
   * This is also how another page links *into* a customer: there is no get-by-id endpoint, so an
   * order's "View profile" navigates here with the buyer's email as the term, and the effect below
   * opens the record when the term matches exactly one.
   */
  readonly q = input<string>();

  protected readonly isLoading = this.facade.isLoading;
  protected readonly isEmpty = this.facade.isEmpty;
  protected readonly error = this.facade.error;
  protected readonly rows = this.facade.rows;
  protected readonly heading = this.facade.heading;
  protected readonly pageSize = PAGE_SIZE;

  /** The region the export captures. Absent until the first response renders it. */
  protected readonly report = viewChild('report', {read: ElementRef});

  /*
   * Bound once as fields rather than passed as `facade.x.bind(facade)` in the template: a method
   * reference created in a binding is a new function every change detection, which makes the
   * dialog's inputs look changed on every tick.
   */
  protected readonly statusLabel = (status: string | undefined) => this.facade.statusLabel(status);
  protected readonly statusTone = (status: string | undefined) => this.facade.statusTone(status);
  protected readonly orderTotal = (order: CustomerOrderRow) => this.facade.orderTotal(order);

  /**
   * Whether the auto-open above has had its one chance for the current term.
   *
   * Resets whenever the term changes, which is what makes it one-shot rather than permanent.
   */
  private readonly autoOpened = linkedSignal<string, boolean>({
    source: () => this.facade.search(),
    computation: () => false,
  });

  protected readonly columns = computed<readonly TableColumn[]>(() => {
    this.transloco.activeLang();
    return COLUMN_KEYS.map((column) => ({
      key: column.key,
      label: column.labelKey ? this.transloco.translate(column.labelKey) : '',
      width: column.width,
    }));
  });

  constructor() {
    /*
     * The URL is what a reload and a shared link restore the term from. `onSearch` is the other
     * writer, so this only has to carry the cases the page did not cause: a first render, a back
     * button, a link from an order.
     *
     * **The current term is read untracked, and that is load-bearing.** Tracking it would make this
     * effect a second writer racing the first: `onSearch` sets the term and *then* navigates, so an
     * effect woken by its own subject would see the new term beside a URL that had not caught up
     * yet, and would immediately put the old one back. The state leads; this only follows the URL.
     */
    effect(() => {
      const term = this.q() ?? '';
      if (term !== untracked(() => this.facade.search())) {
        this.facade.setSearch(term);
      }
    });

    effect(() => {
      const fromUrl = this.customer();
      const id = fromUrl ? Number(fromUrl) : null;
      if (id !== null && Number.isFinite(id) && id !== untracked(() => this.facade.selectedId())) {
        this.facade.selectRow(id);
      }
    });

    /*
     * A search that lands on exactly one customer opens it.
     *
     * This is what makes a link from an order details page reach a person rather than a filtered
     * list of one — the only route there is, since no endpoint fetches a customer by id.
     *
     * **Once per term, not once per render.** Keyed off `selectedId` it would deadlock: an id in the
     * URL that is not on the loaded page sets the selection without `selected()` finding a row, so
     * nothing opens and the auto-open is suppressed for good. Keyed off `selected()` alone it would
     * fight the operator instead, re-opening the dialog the moment they closed it. A flag that
     * resets with the term does neither.
     *
     * **And it waits for the rows to answer the term.** `isLoading` is not enough on its own: there
     * is a moment where the term has changed, the request has not started, and the previous query's
     * rows are still on screen — the flag would be spent counting them. Found in QA, where a link
     * carrying a term landed on the list without opening the one customer it matched.
     */
    effect(() => {
      if (!this.facade.search() || this.autoOpened() || this.isLoading() || !this.facade.rowsMatchSearch()) {
        return;
      }
      const only = this.rows();
      if (only.length === 1 && !this.facade.selected()) {
        this.open(only[0]!.id);
      }
      this.autoOpened.set(true);
    });
  }

  /**
   * Opening sets the dialog and mirrors the choice into the URL.
   *
   * The state leads and the URL follows: `Router.navigate` has not resolved by the time the next
   * statement runs, so a dialog that waited for the URL to come back around would lag a click
   * behind. The effect above then re-sets the same value, which is a no-op.
   */
  protected open(id: number): void {
    this.facade.selectRow(id);
    void this.router.navigate([], {queryParams: {customer: id}, queryParamsHandling: 'merge'});
  }

  protected closeDialog(): void {
    this.facade.clearSelection();
    void this.router.navigate([], {queryParams: {customer: null}, queryParamsHandling: 'merge'});
  }

  /** The term leads and the URL mirrors it, as with the selection. Paging resets in the facade. */
  protected onSearch(term: string): void {
    this.facade.setSearch(term);
    this.facade.clearSelection();
    void this.router.navigate([], {
      queryParams: {q: term || null, customer: null},
      queryParamsHandling: 'merge',
    });
  }

  protected onPage(page: number): void {
    this.facade.goToPage(page);
  }

  protected openOrder(orderId: number): void {
    void this.router.navigate(['/orders', orderId]);
  }

  /** Every order of the open customer, on the page built for reading orders. */
  protected viewAllOrders(): void {
    const id = this.facade.selectedId();
    if (id !== null) {
      void this.router.navigate(['/orders'], {queryParams: {customerId: id}});
    }
  }
}
