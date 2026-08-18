import {computed, inject, Injectable, linkedSignal, signal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import type {ActionItem} from '@shared/ui/action-list/action-list';
import type {DonutSlice} from '@shared/ui/charts/donut-chart';
import type {KpiDatum} from '@shared/ui/kpi-card/kpi-card';
import type {DashboardSnapshot} from '@models/dashboard';
import type {RankedItem} from '@shared/ui/ranked-list/ranked-list';
import type {DateRangeValue} from '@shared/ui/date-range-picker/date-range-picker';
import {DashboardApi, type CompleteRange} from '../services/dashboard.api.service';

/** How far back the dashboard looks when the page opens. */
const DEFAULT_RANGE_DAYS = 30;

/** What a KPI with no source shows in place of a figure. */
const NO_FIGURE = '—';

const DAY_MS = 24 * 60 * 60 * 1000;

/**
 * The last 30 days, ending today.
 *
 * Computed rather than fixed. The previous revision hardcoded July–August 2026, which was harmless
 * against a fixture and simply wrong against real orders.
 */
function defaultRange(): DateRangeValue {
  const to = new Date();
  return {from: new Date(to.getTime() - (DEFAULT_RANGE_DAYS - 1) * DAY_MS), to};
}

/**
 * The dashboard page's data, keyed on the selected reporting period.
 *
 * Changing the range is a new request: the resource re-runs, `isLoading` turns on, and
 * every widget re-renders from the response. The console chrome belongs to
 * `ConsoleShellFacade` and is none of this page's business.
 */
@Injectable({providedIn: 'root'})
export class DashboardFacade {
  private readonly api = inject(DashboardApi);
  private readonly transloco = inject(TranslocoService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly localeFormat = inject(TranslocoLocaleService);

  /**
   * `computed()` rather than a static object: it renders once per page load but must still
   * follow a language switch, and a plain field would freeze at whatever language was
   * active when this root-singleton facade was built.
   */
  readonly heading = computed(() => {
    this.transloco.activeLang();
    return {
      title: this.transloco.translate('dashboard.heading.title'),
      // The open store's name — real data, read from the shell rather than hardcoded. The date
      // follows the active locale like everything else.
      context: this.transloco.translate('dashboard.heading.context', {
        store: this.shell.currentStore()?.name ?? '',
        date: this.localeFormat.localizeDate(this.completeRange()?.to ?? new Date(), undefined, {
          dateStyle: 'medium',
        }),
      }),
    };
  });

  /** The requested period. Writing to it triggers a fetch. */
  readonly dateRange = signal<DateRangeValue>(defaultRange());

  /**
   * The range, once both ends are chosen.
   *
   * `undefined` while a selection is half-made, which leaves the resource idle: the picker emits
   * `{from, to: null}` between the two clicks, and a request built from that would either fail or
   * silently report on the wrong window.
   */
  private readonly completeRange = computed<CompleteRange | undefined>(() => {
    const {from, to} = this.dateRange();
    return from && to ? {from, to} : undefined;
  });

  /**
   * What the page is a reading of: a period **and** a store.
   *
   * The store id is in the params even though no argument is built from it — the request context
   * stamps `?store=` itself. Without it the resource does not re-run when the rail switches stores,
   * and the page keeps one store's figures under another store's name, which is the worst kind of
   * wrong: it looks fine.
   */
  private readonly query = computed(() => {
    const range = this.completeRange();
    const storeId = this.shell.currentStoreId();
    return range && storeId ? {range, storeId} : undefined;
  });

  private readonly snapshot = rxResource({
    params: () => this.query(),
    stream: ({params}) => this.api.loadSnapshot(params.range),
  });

  /**
   * The last snapshot that loaded successfully.
   *
   * A resource clears its value while the next request is in flight, which would blank
   * every widget on each range change. Holding the previous response lets the page stay
   * readable under the loading veil, and avoids the layout collapsing and re-expanding.
   *
   * `hasValue()` guards the read: `value()` throws while the resource is in an error
   * state, and a failed refresh should leave the last good figures on screen.
   */
  private readonly loaded = linkedSignal<DashboardSnapshot | undefined, DashboardSnapshot | undefined>({
    source: () => (this.snapshot.hasValue() ? this.snapshot.value() : undefined),
    computation: (incoming, previous) => incoming ?? previous?.value,
  });

  readonly isLoading = this.snapshot.isLoading;
  readonly error = computed(() => this.snapshot.error() as Error | undefined);
  /** True only before the first response, when there is nothing to show yet. */
  readonly isEmpty = computed(() => this.loaded() === undefined);

  /**
   * Every widget input below reads `transloco.activeLang()` before translating, so a
   * language switch invalidates them the same way a new snapshot does.
   */
  readonly kpis = computed<readonly KpiDatum[]>(() => {
    this.transloco.activeLang();
    return (this.loaded()?.kpis ?? []).map((kpi) => ({
      label: this.transloco.translate(kpi.labelKey),
      // An em dash, never a zero: two of these tiles have no source at all, and a third reports
      // nothing when payments is unreachable.
      value: kpi.value ?? NO_FIGURE,
      icon: kpi.icon,
      tone: kpi.tone,
      delta: kpi.delta,
      trend: kpi.trend,
      flag: kpi.flagKey ? this.transloco.translate(kpi.flagKey) : undefined,
    }));
  });

  readonly attention = computed<readonly ActionItem[]>(() => {
    this.transloco.activeLang();
    return (this.loaded()?.attention ?? []).map((item) => ({
      label: this.transloco.translate(item.labelKey),
      detail: this.transloco.translate(item.detailKey),
      count: item.count ?? NO_FIGURE,
      icon: item.icon,
      tone: item.tone,
    }));
  });

  /** Labels arrive resolved — the server's status names are not translation keys. */
  readonly orderStatuses = computed(() => this.loaded()?.orderStatuses ?? []);

  /** Slices are countries as the orders recorded them, so there is nothing to translate. */
  readonly customerSplit = computed<readonly DonutSlice[]>(() => this.loaded()?.customerSplit ?? []);

  /** SKU and order count — see `Product`. Neither is copy. */
  readonly topProducts = computed<readonly RankedItem[]>(() =>
    (this.loaded()?.products ?? []).map((product) => ({
      label: product.sku,
      value: product.orders,
    })),
  );

  retry(): void {
    this.snapshot.reload();
  }
}
