import {Component, ElementRef, computed, inject, viewChild} from '@angular/core';
import {Router} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {TranslocoDatePipe, TranslocoLocaleService} from '@jsverse/transloco-locale';

import {Badge} from '@shared/ui/badge/badge';
import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {ConfirmDialog} from '@shared/ui/confirm-dialog/confirm-dialog';
import {CopyField} from '@shared/ui/copy-field/copy-field';
import {DataTable, type TableColumn} from '@shared/ui/data-table/data-table';
import {TableRow} from '@shared/ui/data-table/table-row';
import {DateRangePicker} from '@shared/ui/date-range-picker/date-range-picker';
import {EmptyState} from '@shared/ui/empty-state/empty-state';
import {ExportButton} from '@shared/ui/export-button/export-button';
import {Icon} from '@shared/ui/icon/icon';
import {KpiGrid} from '@shared/ui/kpi-grid/kpi-grid';
import {LoadError} from '@shared/ui/load-error/load-error';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {Pagination} from '@shared/ui/pagination/pagination';
import {Panel} from '@shared/ui/panel/panel';
import {SearchBox} from '@shared/ui/search-box/search-box';
import {Select, type SelectOption} from '@shared/ui/select/select';
import {TabSwitcher} from '@shared/ui/tab-switcher/tab-switcher';
import {ApproveDialog} from './components/approve-dialog/approve-dialog';
import {OrderSummaryDialog} from './components/order-summary-dialog/order-summary-dialog';
import {PaymentsFacade, PAGE_SIZE} from './facades/payments.facade';

/** The ledger's columns. Widths are grid tracks, read straight into the row layout. */
const COLUMN_KEYS: readonly {key: string; labelKey: string; width: string; align?: 'start' | 'end'}[] = [
  {key: 'transaction', labelKey: 'payments.column.transaction', width: 'minmax(11rem, 1.4fr)'},
  {key: 'order', labelKey: 'payments.column.order', width: 'minmax(6rem, 0.8fr)'},
  {key: 'method', labelKey: 'payments.column.method', width: 'minmax(7.5rem, 1fr)'},
  {key: 'status', labelKey: 'payments.column.status', width: 'minmax(7.5rem, 1fr)'},
  {key: 'amount', labelKey: 'payments.column.amount', width: '7rem', align: 'end'},
  {key: 'date', labelKey: 'payments.column.date', width: 'minmax(6.5rem, 1fr)'},
  {key: 'actions', labelKey: '', width: '6rem'},
];

/**
 * The payments ledger.
 *
 * Every transaction the store has taken, filterable, with the manual-transfer approval queue
 * leading the tab strip because it is the one tab that is a to-do list.
 *
 * What the design has and this does not: a volume chart, a gateway breakdown, a settlement summary,
 * a payouts panel, a disputes tile, a customer column, a card brand and a per-row fee. The payment
 * service aggregates nothing and a transaction carries none of those fields — see lessons.md.
 * Gateway credentials are not here either; they are store management's Payments section, which the
 * header links to, exactly as the template's primary action does.
 */
@Component({
  selector: 'app-payments',
  imports: [
    ApproveDialog,
    Badge,
    BusyOverlay,
    ConfirmDialog,
    CopyField,
    DataTable,
    DateRangePicker,
    EmptyState,
    ExportButton,
    Icon,
    KpiGrid,
    LoadError,
    OrderSummaryDialog,
    PageHeader,
    Pagination,
    Panel,
    SearchBox,
    Select,
    TabSwitcher,
    TableRow,
    TranslocoDatePipe,
    TranslocoDirective,
  ],
  providers: [PaymentsFacade],
  templateUrl: './payments.html',
  styleUrl: './payments.css',
})
export class Payments {
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);
  private readonly router = inject(Router);

  protected readonly facade = inject(PaymentsFacade);

  protected readonly heading = this.facade.heading;
  protected readonly dateRange = this.facade.dateRange;
  protected readonly activeTab = this.facade.activeTab;
  protected readonly gateway = this.facade.gateway;
  protected readonly search = this.facade.search;

  protected readonly isLoading = this.facade.isLoading;
  protected readonly isEmpty = this.facade.isEmpty;
  protected readonly error = this.facade.error;

  protected readonly kpis = this.facade.kpis;
  protected readonly transactions = this.facade.transactions;
  protected readonly page = this.facade.page;
  protected readonly tabs = this.facade.tabs;
  protected readonly subtitle = this.facade.subtitle;
  protected readonly approving = this.facade.approving;
  protected readonly rejecting = this.facade.rejecting;
  protected readonly busy = this.facade.busy;
  protected readonly pageSize = PAGE_SIZE;

  protected readonly columns = computed<readonly TableColumn[]>(() => {
    this.transloco.activeLang();
    return COLUMN_KEYS.map((column) => ({
      key: column.key,
      label: column.labelKey ? this.transloco.translate(column.labelKey) : '',
      width: column.width,
      align: column.align,
    }));
  });

  /**
   * The gateway filter's options.
   *
   * The four `PaymentType` values, named through the known-set guard. Deliberately **not** loaded
   * from `GET …/supported-payment-types`: that endpoint is unscoped, it answers the same four for
   * every caller, and making the filter wait on it would add a request to every page load for a
   * list the console already types. `PAYPAL` is among them though no PayPal processor exists — see
   * lessons.md, "Payments — a gateway is offered that cannot take money".
   */
  protected readonly gatewayOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return [
      {value: '', label: this.transloco.translate('payments.filter.allGateways')},
      ...['COD', 'MANUAL_TRANSFER', 'STRIPE', 'PAYPAL'].map((type) => ({
        value: type,
        label: this.facade.gatewayLabel(type),
      })),
    ];
  });

  /** The region the export captures. Absent until the first response renders it. */
  protected readonly report = viewChild('report', {read: ElementRef});

  /** Names the period on the exported PDF's first page. */
  protected readonly exportSubtitle = computed(() => {
    const {from, to} = this.dateRange();
    if (!from || !to) {
      return this.heading().context;
    }
    const format = (date: Date) => this.localeFormat.localizeDate(date, undefined, {dateStyle: 'medium'});
    return `${format(from)} – ${format(to)}`;
  });

  /**
   * Leaves the summary for the order page.
   *
   * Closes the dialog first: coming back to the ledger with a modal still open over it would be a
   * surprise, and the route change is the operator choosing to leave.
   */
  protected openFullOrder(): void {
    const pending = this.facade.summaryFor();
    if (pending) {
      this.facade.closeOrderSummary();
      void this.router.navigate(['/orders', pending.id]);
    }
  }

  protected openGatewaySettings(): void {
    this.router.navigate(['/store-management', 'payments']);
  }

  protected onSearch(term: string): void {
    this.facade.search.set(term);
  }

  protected onPage(page: number): void {
    this.facade.goToPage(page);
  }
}
