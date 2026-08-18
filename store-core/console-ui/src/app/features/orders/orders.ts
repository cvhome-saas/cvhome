import {Component, ElementRef, computed, inject, signal, viewChild} from '@angular/core';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import {Badge} from '@shared/ui/badge/badge';
import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {DataTable, type TableColumn} from '@shared/ui/data-table/data-table';
import {TableRow} from '@shared/ui/data-table/table-row';
import {DateRangePicker} from '@shared/ui/date-range-picker/date-range-picker';
import {ExportButton} from '@shared/ui/export-button/export-button';
import {Icon} from '@shared/ui/icon/icon';
import {KpiGrid} from '@shared/ui/kpi-grid/kpi-grid';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {Pagination} from '@shared/ui/pagination/pagination';
import {Panel} from '@shared/ui/panel/panel';
import {TabSwitcher} from '@shared/ui/tab-switcher/tab-switcher';
import {ToastService} from '@shared/ui/toast/toast';
import {
  ORDER_CHANNEL_ICON,
  ORDER_CHANNEL_LABEL_KEY,
  ORDER_STATUS_LABEL_KEY,
  PAYMENT_BADGE,
  PAYMENT_STATE_LABEL_KEY,
  STATUS_TONE,
  type ChannelFilter,
  type OrderRow,
} from '@models/orders';
import {OrdersFacade} from './facades/orders.facade';

/** The order book's columns. Widths are grid tracks, read straight into the row layout. */
const COLUMN_KEYS: readonly {key: string; labelKey: string; width: string; align?: 'start' | 'end'}[] = [
  {key: 'order', labelKey: 'orders.column.order', width: 'minmax(7.5rem, 0.9fr)'},
  {key: 'customer', labelKey: 'orders.column.customer', width: 'minmax(10rem, 1.5fr)'},
  {key: 'status', labelKey: 'orders.column.status', width: 'minmax(7rem, 1fr)'},
  {key: 'payment', labelKey: 'orders.column.payment', width: 'minmax(8rem, 1.05fr)'},
  {key: 'items', labelKey: 'orders.column.items', width: '4.5rem', align: 'end'},
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
    Badge,
    BusyOverlay,
    DataTable,
    DateRangePicker,
    ExportButton,
    Icon,
    KpiGrid,
    NoticeBar,
    PageHeader,
    Pagination,
    Panel,
    TabSwitcher,
    TableRow,
    TranslocoDirective,
  ],
  templateUrl: './orders.html',
  styleUrl: './orders.css',
})
export class Orders {
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);

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
  protected readonly showLateNotice = this.facade.showLateNotice;
  protected readonly lateNotice = this.facade.lateNotice;

  protected readonly channels = this.facade.channels;
  protected readonly channelLabel = this.facade.channelLabel;
  protected readonly pageSize = 10;

  protected readonly channelOpen = signal(false);

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
  protected readonly report = viewChild<ElementRef<HTMLElement>>('report');

  /** Names the period on the exported PDF's first page. */
  protected readonly exportSubtitle = computed(() => {
    const {from, to} = this.dateRange();
    if (!from || !to) {
      return this.heading().context;
    }
    const format = (date: Date) => this.localeFormat.localizeDate(date, undefined, {dateStyle: 'medium'});
    return `${format(from)} – ${format(to)}`;
  });

  protected toneOf(order: OrderRow) {
    return STATUS_TONE[order.status];
  }

  protected paymentOf(order: OrderRow) {
    return PAYMENT_BADGE[order.payment];
  }

  protected channelIconOf(order: OrderRow) {
    return ORDER_CHANNEL_ICON[order.channel];
  }

  protected statusLabelKey(order: OrderRow): string {
    return ORDER_STATUS_LABEL_KEY[order.status];
  }

  protected paymentLabelKey(order: OrderRow): string {
    return PAYMENT_STATE_LABEL_KEY[order.payment];
  }

  protected channelLabelKeyOf(order: OrderRow): string {
    return ORDER_CHANNEL_LABEL_KEY[order.channel];
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

  protected pickChannel(channel: ChannelFilter): void {
    this.facade.channel.set(channel);
    this.channelOpen.set(false);
  }

  protected onPage(page: number): void {
    this.facade.goToPage(page);
  }

  /*
   * Order details, fulfilment and order creation have no backend yet. These say so rather
   * than failing silently, so a click never looks like a bug.
   */
  protected openOrder(order: OrderRow): void {
    this.toast.info(this.transloco.translate('orders.notAvailable.details', {id: order.id}));
  }

  protected createOrder(): void {
    this.toast.info(this.transloco.translate('orders.notAvailable.create'));
  }

  protected shipOrder(order: OrderRow): void {
    this.toast.info(this.transloco.translate('orders.notAvailable.ship', {id: order.id}));
  }

  protected printInvoice(order: OrderRow): void {
    this.toast.info(this.transloco.translate('orders.notAvailable.invoice', {id: order.id}));
  }

  protected markProcessed(): void {
    this.toast.info(this.transloco.translate('orders.notAvailable.bulkFulfilment'));
  }

  protected printPickingLists(): void {
    this.toast.info(this.transloco.translate('orders.notAvailable.pickingLists'));
  }
}
