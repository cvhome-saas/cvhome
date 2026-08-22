import {computed, inject, Injectable, linkedSignal, signal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';

import type {PageT} from '@core/table/table.types';
import {ORDER_STATUSES} from '@models/checkout';
import type {OrderRow, OrderTab, OrdersSnapshot} from '@models/orders';
import {StatusLabel} from '@shared/i18n/status-label';
import type {KpiDatum} from '@shared/ui/kpi-card/kpi-card';
import type {TabItem} from '@shared/ui/tab-switcher/tab-switcher';
import type {DateRangeValue} from '@shared/ui/date-range-picker/date-range-picker';
import {OrdersApi} from '../services/orders.api.service';

/** How far back the page looks when it opens. */
const DEFAULT_RANGE_DAYS = 30;

const DAY_MS = 24 * 60 * 60 * 1000;

/** The last 30 days, ending today — computed, not the fixed 2026 dates this used to carry. */
function defaultRange(): DateRangeValue {
  const to = new Date();
  return {from: new Date(to.getTime() - (DEFAULT_RANGE_DAYS - 1) * DAY_MS), to};
}

/** What a KPI with no source shows in place of a figure. */
const NO_FIGURE = '—';

export const PAGE_SIZE = 10;

const EMPTY_ORDERS: PageT<OrderRow> = {
  size: PAGE_SIZE,
  totalElements: 0,
  totalPages: 0,
  pageNumber: 0,
  content: [],
};

/**
 * The tab strip: every order status, in fulfilment order, plus the unfiltered view.
 *
 * Ten statuses rather than the mockup's five, because ten is what `OrderStatus` has and the endpoint
 * filters on exactly one at a time — grouping them into five would mean inventing groups the API
 * cannot express. The strip scrolls horizontally instead.
 *
 * Labels are humanized, not translated: the server owns this enum, and Transloco throws on a missing
 * key, so an eleventh status would take the page down.
 */
const TABS: readonly OrderTab[] = ['all', ...ORDER_STATUSES];

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
  private readonly localeFormat = inject(TranslocoLocaleService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly statusLabels = inject(StatusLabel);

  readonly dateRange = signal<DateRangeValue>(defaultRange());
  readonly activeTab = signal<OrderTab>('all');
  /** Free-text search, routed to the server's name, email or phone filter by its shape. */
  readonly search = signal('');

  /**
   * One customer's orders, or null for everyone's.
   *
   * Set from `?customerId=` when the customers page hands off "view all orders". An equality on the
   * column checkout writes at placement, so unlike the search term it cannot over-match — which is
   * the whole reason the parameter was bound. Null is the ordinary state of this page.
   */
  readonly customerId = signal<number | null>(null);

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
    source: () => [
      this.dateRange(),
      this.activeTab(),
      this.search(),
      this.customerId(),
      this.shell.currentStoreId(),
    ],
    computation: () => 0,
  });

  /**
   * What the table is a reading of: filters, a page, and a store.
   *
   * `undefined` until the store is known, which leaves the resource idle. The store directory
   * resolves a moment after the page first renders, so without this the page fired **two** requests
   * on every open — one unscoped, one correct.
   *
   * The store id is in the params though no argument is built from it: the request context stamps
   * `?store=` itself, and without it here the table would keep one store's orders under another
   * store's name.
   */
  private readonly query = computed(() => {
    const storeId = this.shell.currentStoreId();
    if (!storeId) {
      return undefined;
    }
    return {
      range: this.dateRange(),
      tab: this.activeTab(),
      search: this.search(),
      customerId: this.customerId(),
      page: {page: this.pageIndex(), count: PAGE_SIZE},
      storeId,
    };
  });

  private readonly snapshot = rxResource({
    params: () => this.query(),
    stream: ({params}) => this.api.loadSnapshot(params),
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
      // An em dash, never a zero: average order value has no source, and two more tiles report
      // nothing while the statistic endpoint is down.
      value: this.figure(kpi.value),
      icon: kpi.icon,
      tone: kpi.tone,
      delta: kpi.delta,
      trend: kpi.trend,
      flag: kpi.flagKey ? this.transloco.translate(kpi.flagKey) : undefined,
    }));
  });
  readonly page = computed<PageT<OrderRow>>(() => this.loaded()?.page ?? EMPTY_ORDERS);
  readonly orders = computed<readonly OrderRow[]>(() => this.page().content);

  /**
   * A KPI's figure in the reader's digits, or an em dash where there is none.
   *
   * The mapping produces plain numeric strings — "12", "4" — and a percentage it has already
   * formatted. Only the plain ones are re-read here; a value that is not a bare number is left
   * exactly as the mapping wrote it.
   */
  private figure(value: string | null): string {
    if (value === null) {
      return NO_FIGURE;
    }
    const numeric = Number(value);
    return Number.isFinite(numeric) && value.trim() !== ''
      ? this.localeFormat.localizeNumber(numeric, 'decimal')
      : value;
  }

  /**
   * What the table is showing right now, under the panel title.
   *
   * A count and a range rather than a sentence: the page header already says how many orders the
   * period holds, so restating it in prose here told the operator nothing. This moves with the
   * filter and with paging, which is the question the line is actually next to.
   */
  readonly subtitle = computed(() => {
    this.transloco.activeLang();
    const tab = this.activeTab();
    const page = this.page();
    const status = tab === 'all' ? null : this.statusLabels.label(tab);

    if (!page.content.length) {
      return status
        ? this.transloco.translate('orders.subtitle.noneForStatus', {status})
        : this.transloco.translate('orders.subtitle.none');
    }

    // Localised digits: the line reads "عرض ١–١٠ من ١٢" in Arabic, not Latin numerals inside an
    // Arabic sentence.
    const digits = (value: number) => this.localeFormat.localizeNumber(value, 'decimal');
    const from = page.pageNumber * page.size + 1;
    const params = {
      from: digits(from),
      to: digits(from + page.content.length - 1),
      total: digits(page.totalElements),
      // The raw number as well: the plural form is chosen from it, while the digits above are what
      // gets printed.
      count: page.totalElements,
      status,
    };
    return status
      ? this.transloco.translate('orders.subtitle.rangeForStatus', params)
      : this.transloco.translate('orders.subtitle.range', params);
  });

  /** The period line under the page title, once a response says how many orders are in it. */
  readonly context = computed(() => {
    this.transloco.activeLang();
    const total = this.loaded()?.page.totalElements;
    return total === undefined
      ? this.heading().context
      : this.transloco.translate('orders.heading.contextWithCount', {
          store: this.shell.currentStore()?.name ?? '',
          count: total,
        });
  });

  /**
   * The tab strip.
   *
   * No badges. The counts that used to sit here came from a "late orders" figure nothing reports —
   * see lessons.md, "Orders — no stale-order signal".
   */
  readonly tabs = computed<readonly TabItem[]>(() => {
    this.transloco.activeLang();
    return TABS.map((tab) => ({
      key: tab,
      label: tab === 'all' ? this.transloco.translate('orders.tab.all') : this.statusLabels.label(tab),
    }));
  });

  goToPage(page: number): void {
    this.pageIndex.set(page);
  }

  retry(): void {
    this.snapshot.reload();
  }
}
