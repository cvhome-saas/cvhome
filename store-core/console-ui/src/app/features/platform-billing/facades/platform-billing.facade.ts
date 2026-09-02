import {Injectable, computed, inject, linkedSignal, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import type {InvoiceStatus, Money as MoneyAmount, SubscriptionStatus} from '@models/billing';
import {
  AUDIT_EVENT_TONE,
  AUDIT_SOURCES,
  AUDIT_SOURCE_TONE,
  FILTERABLE_AUDIT_EVENTS,
  INVOICE_STATUSES,
  INVOICE_STATUS_TONE,
  SUBSCRIPTION_STATUS_TONE,
  type AuditRow,
  type PlanRecurringValueDto,
  type PlatformInvoiceRow,
  type PlatformSubscriptionRow,
} from '@models/platform-billing';
import type {KpiDatum, Tone} from '@cvhome-saas/ui-kit';
import {Money} from '@shared/i18n/money';
import {PlatformLabel} from '@shared/i18n/platform-label';
import {snapshot} from '@cvhome-saas/ui-kit';
import type {BarDatum} from '@shared/ui/charts/bar-chart';
import type {DonutSlice} from '@shared/ui/charts/donut-chart';
import type {TrendPoint as ChartPoint} from '@shared/ui/charts/trend-chart';
import type {DateRangeValue, NavSection, SelectOption} from '@cvhome-saas/ui-kit/ui';
import {PlatformBillingApi, type CompleteRange} from '../services/platform-billing.api.service';

export const PAGE_SIZE = 20;

/** How far back the Overview looks when the section opens. */
const DEFAULT_RANGE_DAYS = 30;

const DAY_MS = 24 * 60 * 60 * 1000;

/** Minor units to major, the catalogue's own convention. Never a locale concern; `Money` does that. */
const MINOR_UNITS = 100;

/**
 * The tabs, in the order the rail renders them.
 *
 * Overview leads because it is the only one that answers a question without being asked one. The
 * other three are registers, in the order an investigation walks them: who is on what, what they
 * were charged, and what happened to them.
 *
 * `dollar` for the section and not `creditCard`: `/platform/plans` already owns that glyph in the
 * same nav rail, and two identical icons one row apart is worse than either choice on its own.
 */
export const BILLING_SECTIONS: readonly NavSection[] = [
  {key: 'overview', labelKey: 'platform.billing.section.overview', icon: 'dollar'},
  {key: 'subscriptions', labelKey: 'platform.billing.section.subscriptions', icon: 'creditCard'},
  {key: 'invoices', labelKey: 'platform.billing.section.invoices', icon: 'receipt'},
  {key: 'activity', labelKey: 'platform.billing.section.activity', icon: 'clock'},
];

export type BillingSection = 'overview' | 'subscriptions' | 'invoices' | 'activity';

/** The last 30 days, ending today. Computed rather than fixed, so it is right on any day. */
function defaultRange(): DateRangeValue {
  const to = new Date();
  return {from: new Date(to.getTime() - (DEFAULT_RANGE_DAYS - 1) * DAY_MS), to};
}

/**
 * The tones the plan mix cycles through, in tier order.
 *
 * A fixed list rather than a map keyed on plan code, because the catalogue is not a closed set — a
 * plan added in Stripe appears here the moment it is mirrored, and a map would give it no colour and
 * a lookup that throws. The order is stable for a stable catalogue, which is what matters: a donut
 * whose slices changed colour between two loads reads as different data.
 */
const PLAN_TONES: readonly Tone[] = ['green', 'cyan', 'violet', 'blue', 'amber', 'slate'];

/**
 * Billing across every tenant.
 *
 * **Four sections, and only the open one loads.** Each register is keyed on `section()`, so opening
 * the page costs the Overview's four legs and nothing else — and coming back to a tab already read
 * does not re-ask. The same shape the organization detail uses.
 *
 * **Every currency is a separate figure, everywhere.** Nothing on the platform holds an exchange
 * rate, so the revenue KPIs are one tile per currency and the run rate is one bar per plan *per*
 * currency. A single "revenue" number would have to invent a rate to exist.
 *
 * **The filters are the server's.** Every one of them narrows the whole platform rather than the
 * twenty rows on screen, and every one is mirrored into the URL by the page so a filtered register
 * can be linked and survives a reload.
 */
@Injectable()
export class PlatformBillingFacade {
  private readonly api = inject(PlatformBillingApi);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);
  private readonly labels = inject(PlatformLabel);
  private readonly money = inject(Money);

  readonly sections = BILLING_SECTIONS;

  /** The open tab, set by the page from the route. */
  readonly section = signal<BillingSection>('overview');

  /** The store whose detail panel is open, or null. Every register can open one. */
  readonly openStore = signal<string | null>(null);

  /* ------------------------------------------------------------------------ overview ---- */

  /** The Overview's period. Writing to it triggers a fetch. */
  readonly dateRange = signal<DateRangeValue>(defaultRange());

  /**
   * The range, once both ends are chosen.
   *
   * `undefined` while a selection is half-made, which leaves the resource idle: the picker emits
   * `{from, to: null}` between the two clicks, and a request built from that would report on the
   * wrong window.
   */
  private readonly completeRange = computed<CompleteRange | undefined>(() => {
    const {from, to} = this.dateRange();
    return from && to ? {from, to} : undefined;
  });

  private readonly overview = snapshot(
    () => (this.section() === 'overview' ? this.completeRange() : undefined),
    (range) => this.api.loadOverview(range),
  );

  readonly isLoading = this.overview.isLoading;
  readonly error = this.overview.error;
  readonly isEmpty = this.overview.isEmpty;
  readonly reload = () => this.overview.reload();

  readonly heading = computed(() => {
    this.transloco.activeLang();
    return {
      title: this.transloco.translate('platform.billing.heading.title'),
      context: this.transloco.translate('platform.billing.heading.context'),
    };
  });

  /**
   * The money row: one tile per currency the platform actually collected in.
   *
   * A list rather than a fixed set of tiles, and that is the honest shape — a platform trading in
   * one currency gets one tile, and one trading in three gets three. Converting them into a single
   * headline figure would mean inventing an exchange rate.
   *
   * A period with no payments at all gets one placeholder tile rather than an empty row, because a
   * missing row reads as a broken panel while an em dash reads as "nothing came in".
   */
  readonly revenueKpis = computed<readonly KpiDatum[]>(() => {
    this.transloco.activeLang();
    const totals = this.overview.value()?.revenueTotals ?? [];
    if (!totals.length) {
      return [
        {
          label: this.transloco.translate('platform.billing.kpi.revenue.none'),
          value: '—',
          icon: 'dollar',
          tone: 'slate',
        },
      ];
    }
    return totals.map((total) => ({
      label: this.transloco.translate('platform.billing.kpi.revenue.inCurrency', {
        currency: total.currency,
      }),
      value: this.money.account(total.minorUnits / MINOR_UNITS, total.currency),
      icon: 'dollar',
      tone: 'green' as Tone,
    }));
  });

  /**
   * The revenue trend, one line per currency.
   *
   * The console's trend chart draws one series, so the page renders one chart per currency rather
   * than stacking them — stacking money in different units would draw a shape that means nothing.
   */
  readonly revenueSeries = computed<readonly {currency: string; total: string; points: readonly ChartPoint[]}[]>(
    () => {
      this.transloco.activeLang();
      return (this.overview.value()?.revenue ?? []).map((series) => ({
        currency: series.name,
        total: this.money.account(series.total / MINOR_UNITS, series.name),
        points: series.points.map((point) => ({
          key: point.date,
          label: this.localeFormat.localizeDate(point.date, undefined, {month: 'short', day: 'numeric'}),
          // Minor units are the wire's unit and the wrong one to plot: a chart's axis is read as
          // money, so it is converted here and only here.
          value: point.value / MINOR_UNITS,
        })),
      }));
    },
  );

  /**
   * The plan mix, as a share of every subscription on the platform.
   *
   * Counts are summed across statuses per plan, because the donut answers "who is on what" — the
   * lifecycle breakdown is the run-rate table's job, and a donut with a slice per plan *and* status
   * is unreadable at any realistic number of plans.
   *
   * The plan-less stores keep a slice of their own. They are `PENDING` rows with no plan at all, and
   * dropping them would make the ring add up to a smaller platform than there is.
   */
  readonly planMix = computed<readonly DonutSlice[]>(() => {
    this.transloco.activeLang();
    const counts = this.overview.value()?.plans?.counts ?? [];
    const byPlan = new Map<string, {label: string; tier: number; value: number}>();
    for (const count of counts) {
      const key = count.planCode ?? '';
      const existing = byPlan.get(key);
      byPlan.set(key, {
        label: existing?.label ?? count.planDisplayName ?? count.planCode ?? this.noPlanLabel(),
        // A plan-less row sorts first: it is the state before every plan, not after them.
        tier: existing?.tier ?? count.tier ?? -1,
        value: (existing?.value ?? 0) + count.subscriptions,
      });
    }
    return [...byPlan.values()]
      .sort((a, b) => a.tier - b.tier)
      .map((entry, index) => ({
        label: entry.label,
        value: entry.value,
        tone: PLAN_TONES[index % PLAN_TONES.length],
      }));
  });

  /**
   * The annualised run rate, one bar per plan per currency.
   *
   * **Only the `ACTIVE` rows.** The server reports every status separately precisely so this choice
   * is the console's, and a run rate that counted trials would be an optimistic book — the label
   * says "committed" so the two cannot be confused. The trial figure is beside it as its own tile.
   *
   * The bars are already annualised by the server; nothing here divides, because dividing a yearly
   * price by twelve truncates on every row.
   */
  readonly runRate = computed<readonly BarDatum[]>(() => {
    this.transloco.activeLang();
    return this.recurring('ACTIVE').map((entry, index) => ({
      label: this.runRateLabel(entry),
      value: (entry.annual?.minorUnits ?? 0) / MINOR_UNITS,
      tone: PLAN_TONES[index % PLAN_TONES.length],
    }));
  });

  /** The committed run rate written out per currency, so the chart's bare numbers have their unit. */
  readonly runRateTotals = computed<readonly {label: string; annual: string; monthly: string}[]>(() => {
    this.transloco.activeLang();
    const byCurrency = new Map<string, {annual: number; monthly: number}>();
    for (const entry of this.recurring('ACTIVE')) {
      const code = entry.annual?.currency.code ?? entry.monthly?.currency.code;
      if (!code) {
        continue;
      }
      const running = byCurrency.get(code) ?? {annual: 0, monthly: 0};
      byCurrency.set(code, {
        annual: running.annual + (entry.annual?.minorUnits ?? 0),
        monthly: running.monthly + (entry.monthly?.minorUnits ?? 0),
      });
    }
    return [...byCurrency.entries()].map(([code, sums]) => ({
      label: code,
      annual: this.money.account(sums.annual / MINOR_UNITS, code),
      monthly: this.money.account(sums.monthly / MINOR_UNITS, code),
    }));
  });

  /** How many stores are trialling right now — the optimistic half of the book, kept separate. */
  readonly trialingCount = computed(() =>
    this.localeFormat.localizeNumber(this.countOf('TRIALING'), 'decimal'),
  );

  readonly activeCount = computed(() =>
    this.localeFormat.localizeNumber(this.countOf('ACTIVE'), 'decimal'),
  );

  /** The first page of the stores billing has cut off. The register's own tab has the rest. */
  readonly blockedRows = computed<readonly PlatformSubscriptionRow[]>(
    () => this.overview.value()?.blocked ?? [],
  );

  readonly blockedTotal = computed(() => this.overview.value()?.blockedTotal ?? 0);

  readonly blockedTotalLabel = computed(() =>
    this.localeFormat.localizeNumber(this.blockedTotal(), 'decimal'),
  );

  /**
   * Whether billing itself is complaining, and about what.
   *
   * Null while it has not been read, and null when it could not be — the leg is optional. The panel
   * renders nothing rather than two zeros in that case, because "no failures" and "we could not ask"
   * are different claims.
   */
  readonly health = computed(() => this.overview.value()?.health ?? null);

  readonly healthy = computed(() => {
    const health = this.health();
    return !!health && health.failedEvents === 0 && health.stalledRequests === 0;
  });

  /* ------------------------------------------------------------------- subscriptions ---- */

  readonly subscriptionSearch = signal('');
  readonly subscriptionStatus = signal('');
  readonly subscriptionPlan = signal('');
  readonly subscriptionOrg = signal('');
  readonly blockedOnly = signal(false);

  /** Narrowing drops the reader back to the first page — page 4 of a smaller result is nothing. */
  readonly subscriptionPage = linkedSignal<unknown, number>({
    source: () =>
      [
        this.subscriptionSearch(),
        this.subscriptionStatus(),
        this.subscriptionPlan(),
        this.subscriptionOrg(),
        this.blockedOnly(),
      ] as const,
    computation: () => 0,
  });

  private readonly subscriptions = snapshot(
    () =>
      this.section() === 'subscriptions'
        ? {
            page: this.subscriptionPage(),
            term: this.subscriptionSearch(),
            status: this.subscriptionStatus() as SubscriptionStatus | '',
            planCode: this.subscriptionPlan(),
            org: this.subscriptionOrg(),
            blockedOnly: this.blockedOnly(),
          }
        : undefined,
    (query) => this.api.loadSubscriptions(query, query.page, PAGE_SIZE),
  );

  readonly subscriptionRows = computed<readonly PlatformSubscriptionRow[]>(
    () => this.subscriptions.value()?.rows ?? [],
  );
  readonly subscriptionsLoading = this.subscriptions.isLoading;
  readonly subscriptionsError = this.subscriptions.error;
  readonly subscriptionsTotal = computed(() => this.subscriptions.value()?.totalElements ?? 0);
  readonly subscriptionsPages = computed(() => this.subscriptions.value()?.totalPages ?? 0);
  readonly reloadSubscriptions = () => this.subscriptions.reload();
  readonly subscriptionsFiltered = computed(
    () =>
      !!this.subscriptionSearch().trim() ||
      !!this.subscriptionStatus() ||
      !!this.subscriptionPlan() ||
      !!this.subscriptionOrg() ||
      this.blockedOnly(),
  );

  /**
   * Whether the rows on screen answer the term in the box.
   *
   * False while a new term is in flight, because the last good rows stay up in the meantime. Only
   * the empty state reads it — telling an operator "nothing matched" over the previous query's
   * results is the mistake this prevents.
   */
  readonly subscriptionRowsMatchSearch = computed(
    () => this.subscriptions.value()?.term === this.subscriptionSearch(),
  );

  /* ------------------------------------------------------------------------- invoices ---- */

  readonly invoiceStore = signal('');
  readonly invoiceOrg = signal('');
  readonly invoiceStatus = signal('');
  readonly invoiceRange = signal<DateRangeValue>({from: null, to: null});

  readonly invoicePage = linkedSignal<unknown, number>({
    source: () =>
      [this.invoiceStore(), this.invoiceOrg(), this.invoiceStatus(), this.invoiceRange()] as const,
    computation: () => 0,
  });

  /**
   * The ledger's filter, as both the rows and the totals read it.
   *
   * One computed rather than two literals: a sum computed over a wider filter than the rows on
   * screen is worse than no sum, because it looks authoritative.
   */
  private readonly invoiceFilter = computed(() => ({
    store: this.invoiceStore().trim(),
    org: this.invoiceOrg(),
    status: this.invoiceStatus() as InvoiceStatus | '',
    from: instantOf(this.invoiceRange().from, 'start'),
    to: instantOf(this.invoiceRange().to, 'end'),
  }));

  private readonly invoices = snapshot(
    () =>
      this.section() === 'invoices'
        ? {...this.invoiceFilter(), page: this.invoicePage()}
        : undefined,
    (query) => this.api.loadInvoices(query, query.page, PAGE_SIZE),
  );

  /**
   * The totals for the same filter, as a second load.
   *
   * Separate so the rows render while the sums are computed, and so a failure to sum costs the
   * figures rather than the ledger.
   */
  private readonly invoiceTotalsLoad = snapshot(
    () => (this.section() === 'invoices' ? this.invoiceFilter() : undefined),
    (query) => this.api.loadInvoiceTotals(query),
  );

  readonly invoiceRows = computed<readonly PlatformInvoiceRow[]>(() => this.invoices.value()?.rows ?? []);
  readonly invoicesLoading = this.invoices.isLoading;
  readonly invoicesError = this.invoices.error;
  readonly invoicesTotal = computed(() => this.invoices.value()?.totalElements ?? 0);
  readonly invoicesPages = computed(() => this.invoices.value()?.totalPages ?? 0);
  readonly reloadInvoices = () => {
    this.invoices.reload();
    this.invoiceTotalsLoad.reload();
  };
  readonly invoicesFiltered = computed(
    () =>
      !!this.invoiceStore().trim() ||
      !!this.invoiceOrg() ||
      !!this.invoiceStatus() ||
      !!this.invoiceRange().from,
  );
  readonly invoiceRowsMatchSearch = computed(
    () => this.invoices.value()?.term === this.invoiceStore().trim(),
  );

  /** One tile per currency in the filtered ledger: what was collected, and what is still owed. */
  readonly invoiceTotals = computed<readonly KpiDatum[]>(() => {
    this.transloco.activeLang();
    const totals = this.invoiceTotalsLoad.value() ?? [];
    return totals.map((total) => {
      const code = total.currency.code;
      const paid = total.paid?.minorUnits ?? 0;
      const due = total.due?.minorUnits ?? 0;
      return {
        label: this.transloco.translate('platform.billing.invoices.total', {currency: code}),
        value: this.money.account(paid / MINOR_UNITS, code),
        icon: 'receipt',
        tone: (paid >= due ? 'green' : 'amber') as Tone,
        // The outstanding half, as the tile's secondary line — a "collected" figure with no
        // "billed" beside it cannot be read as good or bad news.
        flag: this.transloco.translate('platform.billing.invoices.ofBilled', {
          billed: this.money.account(due / MINOR_UNITS, code),
        }),
      } satisfies KpiDatum;
    });
  });

  /* ------------------------------------------------------------------------ activity ---- */

  readonly auditStore = signal('');
  readonly auditOrg = signal('');
  readonly auditEvent = signal('');
  readonly auditSource = signal('');
  readonly auditRange = signal<DateRangeValue>({from: null, to: null});

  readonly auditPage = linkedSignal<unknown, number>({
    source: () =>
      [this.auditStore(), this.auditOrg(), this.auditEvent(), this.auditSource(), this.auditRange()] as const,
    computation: () => 0,
  });

  private readonly audit = snapshot(
    () =>
      this.section() === 'activity'
        ? {
            page: this.auditPage(),
            store: this.auditStore().trim(),
            org: this.auditOrg(),
            eventType: this.auditEvent() as AuditRow['eventType'] | '',
            source: this.auditSource() as AuditRow['source'] | '',
            from: instantOf(this.auditRange().from, 'start'),
            to: instantOf(this.auditRange().to, 'end'),
          }
        : undefined,
    (query) => this.api.loadAudit(query, query.page, PAGE_SIZE),
  );

  readonly auditRows = computed<readonly AuditRow[]>(() => this.audit.value()?.rows ?? []);
  readonly auditLoading = this.audit.isLoading;
  readonly auditError = this.audit.error;
  readonly auditTotal = computed(() => this.audit.value()?.totalElements ?? 0);
  readonly auditPages = computed(() => this.audit.value()?.totalPages ?? 0);
  readonly reloadAudit = () => this.audit.reload();
  readonly auditFiltered = computed(
    () =>
      !!this.auditStore().trim() ||
      !!this.auditOrg() ||
      !!this.auditEvent() ||
      !!this.auditSource() ||
      !!this.auditRange().from,
  );
  readonly auditRowsMatchSearch = computed(() => this.audit.value()?.term === this.auditStore().trim());

  /* -------------------------------------------------------------------- filter options ---- */

  readonly statusOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return [
      {value: '', label: this.transloco.translate('platform.billing.filter.allStatuses')},
      ...(Object.keys(SUBSCRIPTION_STATUS_TONE) as SubscriptionStatus[]).map((status) => ({
        value: status,
        label: this.labels.subscriptionStatus(status),
      })),
    ];
  });

  readonly invoiceStatusOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return [
      {value: '', label: this.transloco.translate('platform.billing.filter.allStatuses')},
      ...INVOICE_STATUSES.map((status) => ({value: status, label: this.labels.invoiceStatus(status)})),
    ];
  });

  /**
   * The event-type filter: thirteen options, not sixteen.
   *
   * The three left out are written by nothing on the platform — see `FILTERABLE_AUDIT_EVENTS`, which
   * names them and says why. An option that can only ever return an empty page reads as a broken
   * filter rather than as an honest absence.
   */
  readonly auditEventOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return [
      {value: '', label: this.transloco.translate('platform.billing.filter.allEvents')},
      ...FILTERABLE_AUDIT_EVENTS.map((event) => ({value: event, label: this.labels.auditEvent(event)})),
    ];
  });

  readonly auditSourceOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return [
      {value: '', label: this.transloco.translate('platform.billing.filter.allSources')},
      ...AUDIT_SOURCES.map((source) => ({value: source, label: this.labels.auditSource(source)})),
    ];
  });

  /**
   * The plan filter, built from the plan statistics the Overview already loaded.
   *
   * Reusing that response rather than fetching the catalogue again: the register's useful options
   * are the plans stores are actually on, which is exactly what the counts enumerate. Before the
   * Overview has loaded the select offers "any plan" alone, which is its resting state anyway.
   */
  readonly planOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    const codes = new Set<string>();
    for (const count of this.overview.value()?.plans?.counts ?? []) {
      if (count.planCode) {
        codes.add(count.planCode);
      }
    }
    return [
      {value: '', label: this.transloco.translate('platform.billing.filter.allPlans')},
      ...[...codes].sort().map((code) => ({value: code, label: code})),
    ];
  });

  /* ----------------------------------------------------------------------- rendering ---- */

  statusLabel(status: string | null): string {
    return this.labels.subscriptionStatus(status);
  }

  statusTone(status: string | null): Tone {
    return (status && SUBSCRIPTION_STATUS_TONE[status as SubscriptionStatus]) || 'slate';
  }

  invoiceStatusLabel(status: string | null): string {
    return this.labels.invoiceStatus(status);
  }

  invoiceStatusTone(status: string | null): Tone {
    return (status && INVOICE_STATUS_TONE[status]) || 'slate';
  }

  eventLabel(event: string | null): string {
    return this.labels.auditEvent(event);
  }

  eventTone(event: string | null): Tone {
    return (event && AUDIT_EVENT_TONE[event as AuditRow['eventType']]) || 'slate';
  }

  sourceLabel(source: string | null): string {
    return this.labels.auditSource(source);
  }

  sourceTone(source: string | null): Tone {
    return (source && AUDIT_SOURCE_TONE[source as AuditRow['source']]) || 'slate';
  }

  /** An amount as billing sends it — minor units and an ISO code — in the reader's locale. */
  amount(value: MoneyAmount | null | undefined): string {
    if (!value) {
      return '—';
    }
    return this.money.account(value.minorUnits / MINOR_UNITS, value.currency.code);
  }

  /** A date, or an em dash. Never `DatePipe`: that is pinned to `LOCALE_ID` and ignores the console. */
  date(value: string | null | undefined): string {
    if (!value) {
      return '—';
    }
    return this.localeFormat.localizeDate(value, undefined, {dateStyle: 'medium'});
  }

  /** A date with its time, for the audit trail — two rows a minute apart are a different story. */
  dateTime(value: string | null | undefined): string {
    if (!value) {
      return '—';
    }
    return this.localeFormat.localizeDate(value, undefined, {dateStyle: 'medium', timeStyle: 'short'});
  }

  count(value: number): string {
    return this.localeFormat.localizeNumber(value, 'decimal');
  }

  /**
   * Who did this, or an honest admission that the table does not know.
   *
   * Null on every `API` row written before the actor was threaded through `SubscriptionApi` — the
   * column existed and was passed a literal null at all five human-driven call sites. Historic rows
   * stay null and say so, rather than being attributed to somebody.
   */
  actorLabel(row: AuditRow): string {
    this.transloco.activeLang();
    return row.actor || this.transloco.translate('platform.billing.activity.actorUnknown');
  }

  setSection(section: BillingSection): void {
    this.section.set(section);
  }

  openStorePanel(store: string): void {
    this.openStore.set(store);
  }

  closeStorePanel(): void {
    this.openStore.set(null);
  }

  /**
   * Re-reads whichever registers a write could have changed.
   *
   * A plan change writes a `subscription_audit` row and moves the subscription, so both registers
   * are stale; the ledger is not, because nothing here raises an invoice. Reloading a section that
   * is not open is free — its resource is idle.
   */
  refreshAfterWrite(): void {
    this.subscriptions.reload();
    this.audit.reload();
    this.overview.reload();
  }

  /* ------------------------------------------------------------------------ internals ---- */

  private recurring(status: SubscriptionStatus): readonly PlanRecurringValueDto[] {
    return (this.overview.value()?.plans?.recurringValue ?? []).filter((entry) => entry.status === status);
  }

  private countOf(status: SubscriptionStatus): number {
    return (this.overview.value()?.plans?.counts ?? [])
      .filter((count) => count.status === status)
      .reduce((sum, count) => sum + count.subscriptions, 0);
  }

  /** A run-rate bar's label carries its currency: two bars for one plan are two markets, not two plans. */
  private runRateLabel(entry: PlanRecurringValueDto): string {
    const code = entry.annual?.currency.code ?? entry.monthly?.currency.code ?? '';
    const plan = entry.planCode ?? this.noPlanLabel();
    return code ? `${plan} · ${code}` : plan;
  }

  private noPlanLabel(): string {
    this.transloco.activeLang();
    return this.transloco.translate('platform.billing.overview.noPlan');
  }
}

/**
 * A picked day as the instant the server filters on.
 *
 * The picker hands back a local `Date` at midnight. Sent as-is, the `to` end drops the whole of its
 * own day — the most recent one, and the one an operator is looking at — because the query is
 * `< :to`. So `end` is pushed to the following midnight and the exclusive bound lands correctly.
 */
function instantOf(date: Date | null, edge: 'start' | 'end'): string | null {
  if (!date) {
    return null;
  }
  const at = new Date(date);
  at.setHours(0, 0, 0, 0);
  if (edge === 'end') {
    at.setDate(at.getDate() + 1);
  }
  return at.toISOString();
}
