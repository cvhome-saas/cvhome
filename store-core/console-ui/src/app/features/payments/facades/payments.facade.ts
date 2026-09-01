import {Injectable, computed, inject, linkedSignal, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import type {PageT} from '@cvhome-saas/ui-kit';
import {ApiErrorService, snapshot} from '@cvhome-saas/ui-kit';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import {humanizeStatus, STATUS_TONE} from '@models/orders';
import type {PaymentStatus} from '@models/payment';
import {PAYMENT_TYPE_LABEL_KEY, isPaymentType} from '@models/store-settings';
import {parseAmount, type ReadableOrder} from '@models/checkout';
import {
  PAYMENT_TABS,
  TRANSACTION_TONE,
  type OrderSummary,
  type OrderSummaryLine,
  type PaymentTab,
  type TransactionKpiSource,
  type TransactionRow,
} from '@models/transactions';
import {Money} from '@shared/i18n/money';
import {StatusLabel} from '@shared/i18n/status-label';
import type {KpiDatum} from '@shared/ui/kpi-card/kpi-card';
import type {TabItem} from '@shared/ui/tab-switcher/tab-switcher';
import type {DateRangeValue} from '@shared/ui/date-range-picker/date-range-picker';
import {ToastService} from '@shared/ui/toast/toast';
import {PaymentsApi} from '../services/payments.api.service';

export const PAGE_SIZE = 20;

/** How far back the ledger looks when it opens. */
const DEFAULT_RANGE_DAYS = 30;
const DAY_MS = 24 * 60 * 60 * 1000;

/** What a KPI with no readable count shows in place of a figure. */
const NO_FIGURE = '—';

function defaultRange(): DateRangeValue {
  const to = new Date();
  return {from: new Date(to.getTime() - (DEFAULT_RANGE_DAYS - 1) * DAY_MS), to};
}

const EMPTY_PAGE: PageT<TransactionRow> = {
  size: PAGE_SIZE,
  totalElements: 0,
  totalPages: 0,
  pageNumber: 0,
  content: [],
};

/** A transaction queued for approval or rejection, waiting on its dialog. */
interface Pending {
  readonly internalRef: string;
  readonly reference: string;
}

/**
 * The payments ledger.
 *
 * Two loads, on deliberately different keys.
 *
 * The **table** is keyed on everything the operator can change — the tab, the gateway, the search,
 * the period, the page — because every one of those is a different question for the server.
 *
 * The **KPI counts** are keyed on the store and the period only — never on the tab, whose changes do
 * not move them. `PaymentsApi.loadCounts` is what issues them, and it explains why there are four.
 *
 * The table is the unwrapped leg: if it fails, the page failed and offers a retry. The counts are
 * each optional and render an em dash rather than a zero, because on a page whose whole point is an
 * approval queue, "nothing is waiting" is the most dangerous wrong answer available.
 */
@Injectable()
export class PaymentsFacade {
  private readonly api = inject(PaymentsApi);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly statusLabels = inject(StatusLabel);
  private readonly money = inject(Money);

  readonly dateRange = signal<DateRangeValue>(defaultRange());
  readonly activeTab = signal<PaymentTab>('queue');
  /** A `PaymentType` name, or `''` for every gateway. */
  readonly gateway = signal('');
  /** Free-text search, routed to `requestRef` or `internalRef` by its shape. */
  readonly search = signal('');

  readonly busy = signal(false);
  readonly approving = signal<Pending | null>(null);
  readonly rejecting = signal<Pending | null>(null);

  /**
   * The page being read.
   *
   * A `linkedSignal` over every filter, so narrowing the ledger drops the reader back to the first
   * page rather than asking for page 4 of a two-page result.
   */
  readonly pageIndex = linkedSignal<unknown, number>({
    source: () => [
      this.dateRange(),
      this.activeTab(),
      this.gateway(),
      this.search(),
      this.shell.currentStoreId(),
    ],
    computation: () => 0,
  });

  /**
   * What the table is a reading of.
   *
   * `undefined` until the store is known, which leaves the resource idle. The store directory
   * resolves a moment after the page first renders, and without this gate the page would fire two
   * requests on every open — one unscoped, one correct.
   */
  private readonly query = computed(() => {
    const storeId = this.shell.currentStoreId();
    if (!storeId) {
      return undefined;
    }
    return {
      tab: this.activeTab(),
      gateway: this.gateway(),
      search: this.search(),
      page: {page: this.pageIndex(), count: PAGE_SIZE},
      range: this.dateRange(),
      storeId,
    };
  });

  private readonly ledger = snapshot(
    () => this.query(),
    (query) => this.api.loadSnapshot(query),
  );

  /** The counts' own key: the store and the period, and nothing the tab strip can change. */
  private readonly countsQuery = computed(() => {
    const storeId = this.shell.currentStoreId();
    return storeId ? {range: this.dateRange(), storeId} : undefined;
  });

  private readonly counts = snapshot(
    () => this.countsQuery(),
    ({range}) => this.api.loadCounts(range),
  );

  readonly isLoading = this.ledger.isLoading;
  readonly error = this.ledger.error;
  readonly isEmpty = this.ledger.isEmpty;

  readonly page = computed<PageT<TransactionRow>>(() => this.ledger.value()?.page ?? EMPTY_PAGE);
  readonly transactions = computed<readonly TransactionRow[]>(() => this.page().content);

  readonly heading = computed(() => {
    this.transloco.activeLang();
    return {
      title: this.transloco.translate('payments.heading.title'),
      context: this.transloco.translate('payments.heading.context', {
        store: this.shell.currentStore()?.name ?? '',
      }),
    };
  });

  /**
   * The KPI row: four counts, never four amounts.
   *
   * The template's tiles are Captured $48,230, Pending approval $12,480, Refunded $986 and Disputes
   * open. Nothing on the platform sums a transaction and nothing records a dispute, so three become
   * counts of the same thing and the fourth is replaced by the count that matters operationally —
   * failures. See lessons.md, "Payments — nothing aggregates a transaction".
   */
  readonly kpis = computed<readonly KpiDatum[]>(() => {
    this.transloco.activeLang();
    const read = this.counts.value();
    const sources: readonly TransactionKpiSource[] = [
      {
        labelKey: 'payments.kpi.awaitingApproval',
        value: figureOf(read?.queue),
        icon: 'clock',
        tone: read?.queue ? 'amber' : 'slate',
        flagKey: flagFor(read?.queue, 'payments.kpi.actionNeeded', 'payments.kpi.allClear'),
      },
      {
        labelKey: 'payments.kpi.captured',
        value: figureOf(read?.paid),
        icon: 'checkCircle',
        tone: read === undefined || read.paid === null ? 'slate' : 'green',
        flagKey: read?.paid === null ? 'payments.kpi.unavailable' : undefined,
      },
      {
        labelKey: 'payments.kpi.failed',
        value: figureOf(read?.failed),
        icon: 'xCircle',
        tone: read === undefined || read.failed === null ? 'slate' : 'red',
        flagKey: read?.failed === null ? 'payments.kpi.unavailable' : undefined,
      },
      {
        labelKey: 'payments.kpi.refunded',
        value: figureOf(read?.refunded),
        icon: 'undo',
        tone: read === undefined || read.refunded === null ? 'slate' : 'violet',
        flagKey: read?.refunded === null ? 'payments.kpi.unavailable' : undefined,
      },
    ];

    return sources.map((kpi) => ({
      label: this.transloco.translate(kpi.labelKey),
      value: this.figure(kpi.value),
      icon: kpi.icon,
      tone: kpi.tone,
      flag: kpi.flagKey ? this.transloco.translate(kpi.flagKey) : undefined,
    }));
  });

  /**
   * The tab strip: the approval queue, everything, then each status.
   *
   * Only the queue carries a badge, and only once its count has been read — it is the one tab that
   * is a to-do list rather than a view. Status labels go through the known-set guard, so a value the
   * console has never seen is humanized instead of taking the page down under Transloco's strict
   * missing-key handler.
   */
  readonly tabs = computed<readonly TabItem[]>(() => {
    this.transloco.activeLang();
    const waiting = this.counts.value()?.queue ?? null;
    return PAYMENT_TABS.map((tab) => ({
      key: tab,
      label:
        tab === 'queue'
          ? this.transloco.translate('payments.tab.queue')
          : tab === 'all'
            ? this.transloco.translate('payments.tab.all')
            : this.statusLabels.label(tab),
      badge: tab === 'queue' && waiting ? this.localeFormat.localizeNumber(waiting, 'decimal') : undefined,
      badgeTone: tab === 'queue' && waiting ? ('amber' as const) : undefined,
    }));
  });

  /** What the table is showing right now, under the panel title. */
  readonly subtitle = computed(() => {
    this.transloco.activeLang();
    const page = this.page();
    const tab = this.activeTab();

    if (!page.content.length) {
      return this.transloco.translate('payments.subtitle.none');
    }

    const digits = (value: number) => this.localeFormat.localizeNumber(value, 'decimal');
    const from = page.pageNumber * page.size + 1;
    const params = {
      from: digits(from),
      to: digits(from + page.content.length - 1),
      total: digits(page.totalElements),
      count: page.totalElements,
    };
    return tab === 'queue'
      ? this.transloco.translate('payments.subtitle.queue', params)
      : this.transloco.translate('payments.subtitle.range', params);
  });

  /** Whether the gateway select applies. It does not on the queue tab, which owns `paymentType`. */
  readonly gatewayFilterApplies = computed(() => this.activeTab() !== 'queue');

  readonly hasFilters = computed(
    () => this.search().trim() !== '' || this.gateway() !== '' || this.activeTab() !== 'all',
  );

  tone(status: PaymentStatus) {
    return TRANSACTION_TONE[status] ?? 'slate';
  }

  statusLabel(status: PaymentStatus): string {
    return this.statusLabels.label(status);
  }

  /**
   * A gateway name, translated only when the console knows it.
   *
   * The Module 4 known-set rule applied to `PaymentType`: Transloco throws on a missing key, so a
   * fifth gateway added server-side would take the page down if this looked the key up blind.
   * Membership is checked first and anything unrecognised is humanized. The four keys are store
   * management's own — a gateway is called the same thing on both screens.
   */
  gatewayLabel(paymentType: string): string {
    return isPaymentType(paymentType)
      ? this.transloco.translate(PAYMENT_TYPE_LABEL_KEY[paymentType])
      : humanizeStatus(paymentType);
  }

  amount(row: TransactionRow): string {
    return this.money.format(row.amount.value, row.amount.currency);
  }

  clearFilters(): void {
    this.search.set('');
    this.gateway.set('');
    this.activeTab.set('all');
  }

  goToPage(page: number): void {
    this.pageIndex.set(page);
  }

  reload(): void {
    this.ledger.reload();
  }

  /* ------------------------------------------------------------- the order summary ---- */

  /** The order whose summary is open, or null. Drives both the fetch and the dialog. */
  readonly summaryFor = signal<{id: number; reference: string} | null>(null);

  private readonly orderSummary = snapshot(
    () => this.summaryFor()?.id,
    (orderId) => this.api.loadOrder(orderId),
  );

  readonly summaryLoading = this.orderSummary.isLoading;
  readonly summaryError = this.orderSummary.error;

  /**
   * The order, shaped for the dialog.
   *
   * A `computed` over the loaded order rather than a mapping done once: money, the date and the
   * status label all have to re-read when the operator switches language, and a string mapped at
   * fetch time would keep whichever language was active then.
   */
  readonly summary = computed<OrderSummary | null>(() => {
    this.transloco.activeLang();
    const order = this.orderSummary.value();
    if (!order || this.summaryFor() === null) {
      return null;
    }
    return this.toSummary(order);
  });

  openOrderSummary(row: TransactionRow): void {
    if (row.orderId !== null) {
      this.summaryFor.set({id: row.orderId, reference: `#${row.orderId}`});
    }
  }

  closeOrderSummary(): void {
    this.summaryFor.set(null);
  }

  private toSummary(order: ReadableOrder): OrderSummary {
    const person = order.customer ?? order.billing;
    const name = [person?.firstName, person?.lastName].filter(Boolean).join(' ').trim();
    const email = order.customer?.emailAddress ?? order.billing?.email ?? '';
    const currency = order.currency ?? null;
    const lines: readonly OrderSummaryLine[] = (order.products ?? []).map((product, index) => ({
      id: product.id ?? index,
      name: product.productName ?? product.sku ?? '—',
      quantity: product.orderedQuantity ?? 0,
      lineTotal: this.money.format(parseAmount(product.subTotal), currency),
    }));

    return {
      id: order.id ?? 0,
      reference: order.id === undefined ? '—' : `#${order.id}`,
      status: this.statusLabels.label(order.orderStatus),
      tone: order.orderStatus ? STATUS_TONE[order.orderStatus] : 'slate',
      placedOn: order.datePurchased || null,
      customer: name || email || '—',
      email,
      lines,
      itemCount: lines.reduce((sum, line) => sum + line.quantity, 0),
      /*
       * Both figures are formatted from their amounts, and the server's own `text` is deliberately
       * ignored for both.
       *
       * A line has no usable `text` and the grand total does, so honouring it — which is what the
       * orders page does — put `SAR8,500.00` on the line and `SAR 8,500.00` in the footer of the
       * same small box. Two money figures an inch apart cannot be spaced differently. Formatting
       * both from the amount also makes them locale-correct, which the server's string is not.
       */
      total: this.money.format(order.total?.value ?? null, currency),
    };
  }

  /* ------------------------------------------------------------------- the writes ---- */

  askToApprove(row: TransactionRow): void {
    this.approving.set({internalRef: row.internalRef, reference: row.reference});
  }

  askToReject(row: TransactionRow): void {
    this.rejecting.set({internalRef: row.internalRef, reference: row.reference});
  }

  dismissDialogs(): void {
    this.approving.set(null);
    this.rejecting.set(null);
  }

  /**
   * Confirms a payment.
   *
   * Re-reads rather than patching the row: the endpoint answers `void`, and what the operator should
   * see is what the server recorded, not what the console assumed it would.
   */
  approve(transactionNo: string): void {
    const pending = this.approving();
    if (!pending || this.busy()) {
      return;
    }
    this.busy.set(true);
    this.api.approve(pending.internalRef, {transactionNo}).subscribe({
      next: () => {
        this.busy.set(false);
        this.approving.set(null);
        this.toast.success(
          this.transloco.translate('payments.toast.approved', {reference: pending.reference}),
        );
        this.reloadAll();
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.toast.danger(this.apiErrors.messageFor(failure));
      },
    });
  }

  /**
   * Refuses a payment.
   *
   * The toast says the order does not move, because it does not: `reject` fires no event, so
   * checkout is never told. Saying so at the moment of the action is the only place an operator
   * would find that out. See lessons.md, "Payments — rejecting a payment tells checkout nothing".
   */
  reject(): void {
    const pending = this.rejecting();
    if (!pending || this.busy()) {
      return;
    }
    this.busy.set(true);
    this.api.reject(pending.internalRef).subscribe({
      next: () => {
        this.busy.set(false);
        this.rejecting.set(null);
        this.toast.info(
          this.transloco.translate('payments.toast.rejected', {reference: pending.reference}),
        );
        this.reloadAll();
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.toast.danger(this.apiErrors.messageFor(failure));
      },
    });
  }

  /** A write moves a row between statuses, so both the table and the counts are stale. */
  private reloadAll(): void {
    this.ledger.reload();
    this.counts.reload();
  }

  /**
   * A KPI's figure in the reader's digits, or an em dash where the count could not be read.
   *
   * Distinguishes "not loaded yet or failed" from a genuine zero: `null` becomes the dash, `0`
   * becomes a localised zero under an "all clear" flag.
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
}

/** `undefined` while loading and `null` on a failed leg both mean "no figure". */
function figureOf(count: number | null | undefined): string | null {
  return count === null || count === undefined ? null : String(count);
}

function flagFor(count: number | null | undefined, whenSome: string, whenNone: string): string {
  if (count === null || count === undefined) {
    return 'payments.kpi.unavailable';
  }
  return count > 0 ? whenSome : whenNone;
}
