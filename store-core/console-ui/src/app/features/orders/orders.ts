import {Component, ElementRef, computed, inject, viewChild} from '@angular/core';
import {Router} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {TranslocoDatePipe, TranslocoLocaleService} from '@jsverse/transloco-locale';

import {Badge} from '@shared/ui/badge/badge';
import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {DataTable, type TableColumn} from '@shared/ui/data-table/data-table';
import {TableRow} from '@shared/ui/data-table/table-row';
import {DateRangePicker} from '@shared/ui/date-range-picker/date-range-picker';
import {ExportButton} from '@shared/ui/export-button/export-button';
import {Icon} from '@shared/ui/icon/icon';
import {EmptyState} from '@shared/ui/empty-state/empty-state';
import {LoadError} from '@shared/ui/load-error/load-error';
import {KpiGrid} from '@shared/ui/kpi-grid/kpi-grid';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {Pagination} from '@shared/ui/pagination/pagination';
import {Panel} from '@shared/ui/panel/panel';
import {SearchBox} from '@shared/ui/search-box/search-box';
import {TabSwitcher} from '@shared/ui/tab-switcher/tab-switcher';
import {ToastService} from '@shared/ui/toast/toast';
import {STATUS_TONE, type OrderRow} from '@models/orders';
import {Money} from '@shared/i18n/money';
import {StatusLabel} from '@shared/i18n/status-label';
import {OrdersFacade} from './facades/orders.facade';

/** The order book's columns. Widths are grid tracks, read straight into the row layout. */
const COLUMN_KEYS: readonly {key: string; labelKey: string; width: string; align?: 'start' | 'end'}[] = [
  {key: 'order', labelKey: 'orders.column.order', width: 'minmax(7.5rem, 0.9fr)'},
  {key: 'customer', labelKey: 'orders.column.customer', width: 'minmax(10rem, 1.5fr)'},
  {key: 'city', labelKey: 'orders.column.city', width: 'minmax(7rem, 1fr)'},
  {key: 'status', labelKey: 'orders.column.status', width: 'minmax(7rem, 1fr)'},
  {key: 'payment', labelKey: 'orders.column.payment', width: 'minmax(8rem, 1.05fr)'},
  {key: 'total', labelKey: 'orders.column.total', width: '6rem', align: 'end'},
  {key: 'placed', labelKey: 'orders.column.placed', width: 'minmax(6rem, 1fr)'},
  {key: 'actions', labelKey: '', width: '6rem'},
];

/**
 * The order book.
 *
 * Renders into `ConsoleShell`, which owns the banner, navigation rail and toolbar, so this
 * component is only its own content — a page header, the period's metrics, and the table.
 *
 * Every filter is a data request: the facade refetches and the table goes under a loading
 * veil, keeping the previous rows visible underneath.
 */
@Component({
  selector: 'app-orders',
  imports: [
    SearchBox,
    EmptyState,
    LoadError,
    Badge,
    BusyOverlay,
    DataTable,
    DateRangePicker,
    ExportButton,
    Icon,
    KpiGrid,
    PageHeader,
    Pagination,
    Panel,
    TabSwitcher,
    TableRow,
    TranslocoDatePipe,
    TranslocoDirective,
  ],
  templateUrl: './orders.html',
  styleUrl: './orders.css',
})
export class Orders {
  private readonly toast = inject(ToastService);
  private readonly statusLabels = inject(StatusLabel);
  private readonly money = inject(Money);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);
  private readonly router = inject(Router);

  protected readonly facade = inject(OrdersFacade);

  protected readonly heading = this.facade.heading;
  protected readonly dateRange = this.facade.dateRange;
  protected readonly activeTab = this.facade.activeTab;

  protected readonly isLoading = this.facade.isLoading;
  protected readonly isEmpty = this.facade.isEmpty;
  protected readonly error = this.facade.error;

  protected readonly kpis = this.facade.kpis;
  protected readonly orders = this.facade.orders;
  protected readonly page = this.facade.page;
  protected readonly tabs = this.facade.tabs;
  protected readonly subtitle = this.facade.subtitle;
  protected readonly context = this.facade.context;
  protected readonly search = this.facade.search;
  protected readonly pageSize = 10;

  protected readonly columns = computed<readonly TableColumn[]>(() => {
    this.transloco.activeLang();
    return COLUMN_KEYS.map((column) => ({
      key: column.key,
      label: column.labelKey ? this.transloco.translate(column.labelKey) : '',
      width: column.width,
      align: column.align,
    }));
  });

  /** The region the export captures. Absent until the first response renders it. */
  // Explicitly the element — see the note in `products.ts`.
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

  /** Slate for an order whose status the console has not seen — a colour, not a claim. */
  protected toneOf(order: OrderRow) {
    return order.status ? STATUS_TONE[order.status] : 'slate';
  }

  protected statusLabel(order: OrderRow): string {
    return this.statusLabels.label(order.status);
  }

  /** True when what is on screen is narrowed by something the operator can undo. */
  protected readonly filtered = computed(() => this.activeTab() !== 'all' || this.search().trim() !== '');

  protected clearFilters(): void {
    this.activeTab.set('all');
    this.facade.search.set('');
  }

  protected totalLabel(order: OrderRow): string {
    return this.money.format(order.total.value, order.total.currency, order.total.text);
  }

  protected paymentLabel(order: OrderRow): string {
    return this.statusLabels.label(order.payment);
  }

  /** First letters of the first two words, as the avatar tile shows them. */
  protected initialsOf(order: OrderRow): string {
    return order.customer
      .split(' ')
      .slice(0, 2)
      .map((word) => word.charAt(0))
      .join('')
      .toUpperCase();
  }

  protected onSearch(term: string): void {
    this.facade.search.set(term);
  }

  protected onPage(page: number): void {
    this.facade.goToPage(page);
  }

  protected openOrder(order: OrderRow): void {
    this.router.navigate(['/orders', order.id]);
  }

  /*
   * TODO(lessons.md): creating an order, shipping one and bulk fulfilment have no endpoints — see
   * lessons.md, "Orders — no seller-side order creation", "Orders — no fulfilment or shipping model"
   * and "Orders — no cancel and no duplicate".
   * These say so rather than failing silently, so a click never looks like a bug.
   */
  protected createOrder(): void {
    this.toast.info(this.transloco.translate('orders.notAvailable.create'));
  }

  protected markProcessed(): void {
    this.toast.info(this.transloco.translate('orders.notAvailable.bulkFulfilment'));
  }

  protected printPickingLists(): void {
    this.toast.info(this.transloco.translate('orders.notAvailable.pickingLists'));
  }
}
