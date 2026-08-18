import {computed, inject, Injectable, linkedSignal, signal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {TranslocoService} from '@jsverse/transloco';

import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';

import type {PageT} from '@core/table/table.types';
import type {ChannelFilter, OrderRow, OrderTab, OrdersSnapshot} from '@models/orders';
import type {KpiDatum} from '@shared/ui/kpi-card/kpi-card';
import type {TabItem} from '@shared/ui/tab-switcher/tab-switcher';
import type {DateRangeValue} from '@shared/ui/date-range-picker/date-range-picker';
import {OrdersApi} from '../services/orders.api.service';

const DEFAULT_RANGE: DateRangeValue = {from: new Date(2026, 6, 5), to: new Date(2026, 7, 4)};

export const PAGE_SIZE = 10;

const EMPTY_ORDERS: PageT<OrderRow> = {
  size: PAGE_SIZE,
  totalElements: 0,
  totalPages: 0,
  pageNumber: 0,
  content: [],
};

/** The tab strip, in fulfilment order. Badges are filled in from the loaded data. */
const TABS: readonly {key: OrderTab; labelKey: string}[] = [
  {key: 'all', labelKey: 'orders.tab.all'},
  {key: 'Ordered', labelKey: 'orders.status.ordered'},
  {key: 'Processed', labelKey: 'orders.status.processed'},
  {key: 'Delivered', labelKey: 'orders.status.delivered'},
  {key: 'Refunded', labelKey: 'orders.status.refunded'},
  {key: 'Canceled', labelKey: 'orders.status.canceled'},
];

/** What each tab is showing, said plainly under the panel title. */
const SUBTITLE_KEYS: Readonly<Record<OrderTab, string>> = {
  all: 'orders.subtitle.all',
  Ordered: 'orders.subtitle.ordered',
  Processed: 'orders.subtitle.processed',
  Delivered: 'orders.subtitle.delivered',
  Refunded: 'orders.subtitle.refunded',
  Canceled: 'orders.subtitle.canceled',
};

export const CHANNEL_FILTERS: readonly {key: ChannelFilter; labelKey: string}[] = [
  {key: 'all', labelKey: 'orders.channel.all'},
  {key: 'Web', labelKey: 'orders.channel.web'},
  {key: 'Phone', labelKey: 'orders.channel.phone'},
];

/**
 * The orders page's data, keyed on the period, the status tab, the channel and the page.
 *
 * Every one of those is a new request: the resource re-runs, `isLoading` turns on, and the
 * table re-renders from the response. Filtering and paging happen behind `OrdersApi`, not
 * here, so this reads the same whether the data is mocked or served.
 */
@Injectable({providedIn: 'root'})
export class OrdersFacade {
  private readonly api = inject(OrdersApi);
  private readonly transloco = inject(TranslocoService);
  private readonly shell = inject(ConsoleShellFacade);

  readonly dateRange = signal<DateRangeValue>(DEFAULT_RANGE);
  readonly activeTab = signal<OrderTab>('all');
  readonly channel = signal<ChannelFilter>('all');

  /**
   * `computed()` rather than a static object — see `DashboardFacade.heading` for why:
   * a root-singleton field would freeze at whatever language booted the app.
   */
  readonly heading = computed(() => {
    this.transloco.activeLang();
    return {
      title: this.transloco.translate('orders.heading.title'),
      context: this.transloco.translate('orders.heading.context', {store: this.shell.currentStore()?.name ?? ''}),
    };
  });

  /**
   * The page being read.
   *
   * Written as a `linkedSignal` over the filters so that narrowing the list drops the
   * reader back to the first page. Holding the old index would ask for page 4 of a
   * two-page result — the API clamps it, but the reader would still land somewhere they
   * did not choose.
   */
  readonly pageIndex = linkedSignal<unknown, number>({
    source: () => [this.dateRange(), this.activeTab(), this.channel()],
    computation: () => 0,
  });

  private readonly snapshot = rxResource({
    params: () => ({
      range: this.dateRange(),
      tab: this.activeTab(),
      channel: this.channel(),
      page: {page: this.pageIndex(), count: PAGE_SIZE},
    }),
    stream: ({params}) => this.api.loadOrders(params),
  });

  /**
   * The last snapshot that loaded successfully.
   *
   * A resource clears its value while the next request is in flight, which would empty the
   * table on every tab change. Holding the previous response keeps it readable under the
   * loading veil and stops the panel collapsing and re-expanding.
   *
   * `hasValue()` guards the read: `value()` throws while the resource is in an error state,
   * and a failed refresh should leave the last good rows on screen.
   */
  private readonly loaded = linkedSignal<OrdersSnapshot | undefined, OrdersSnapshot | undefined>({
    source: () => (this.snapshot.hasValue() ? this.snapshot.value() : undefined),
    computation: (incoming, previous) => incoming ?? previous?.value,
  });

  readonly isLoading = this.snapshot.isLoading;
  readonly error = computed(() => this.snapshot.error() as Error | undefined);
  /** True only before the first response, when there is nothing to show yet. */
  readonly isEmpty = computed(() => this.loaded() === undefined);

  readonly kpis = computed<readonly KpiDatum[]>(() => {
    this.transloco.activeLang();
    return (this.loaded()?.kpis ?? []).map((kpi) => ({
      label: this.transloco.translate(kpi.labelKey),
      value: kpi.value,
      icon: kpi.icon,
      tone: kpi.tone,
      delta: kpi.delta,
      flag: kpi.flagKey
        ? this.transloco.translate(kpi.flagKey)
        : kpi.flagCount !== undefined
          ? this.transloco.translate('orders.kpi.refundedCount', {count: kpi.flagCount})
          : undefined,
    }));
  });
  readonly page = computed<PageT<OrderRow>>(() => this.loaded()?.page ?? EMPTY_ORDERS);
  readonly orders = computed<readonly OrderRow[]>(() => this.page().content);
  readonly lateCount = computed(() => this.loaded()?.lateCount ?? 0);

  readonly subtitle = computed(() => {
    this.transloco.activeLang();
    return this.transloco.translate(SUBTITLE_KEYS[this.activeTab()]);
  });

  /** The period line under the page title, once a response says how much is in it. */
  readonly context = computed(() => {
    this.transloco.activeLang();
    const total = this.loaded()?.totalInRange;
    return total === undefined
      ? this.heading().context
      : this.transloco.translate('orders.heading.contextWithCount', {
          store: this.shell.currentStore()?.name ?? '',
          count: total,
        });
  });

  /**
   * The tab strip. Only the tab needing action carries a badge — badging every tab with its
   * count would make the one that matters no louder than the rest.
   */
  readonly tabs = computed<readonly TabItem[]>(() => {
    this.transloco.activeLang();
    const late = this.lateCount();
    return TABS.map((tab) => ({
      key: tab.key,
      label: this.transloco.translate(tab.labelKey),
      badge: tab.key === 'Ordered' && late > 0 ? `${late}` : undefined,
      badgeTone: 'red' as const,
    }));
  });

  /** The overdue notice belongs to the queue those orders are stuck in. */
  readonly showLateNotice = computed(() => this.activeTab() === 'Ordered' && this.lateCount() > 0);

  readonly lateNotice = computed(() => {
    this.transloco.activeLang();
    return this.transloco.translate('orders.lateNotice', {count: this.lateCount()});
  });

  readonly channels = computed(() => {
    this.transloco.activeLang();
    return CHANNEL_FILTERS.map((option) => ({
      key: option.key,
      label: this.transloco.translate(option.labelKey),
    }));
  });

  readonly channelLabel = computed(() => {
    this.transloco.activeLang();
    const active = this.channel();
    return this.transloco.translate(
      CHANNEL_FILTERS.find((option) => option.key === active)?.labelKey ?? 'orders.channel.all',
    );
  });

  goToPage(page: number): void {
    this.pageIndex.set(page);
  }

  retry(): void {
    this.snapshot.reload();
  }
}
