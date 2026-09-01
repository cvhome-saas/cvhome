import {Injectable, computed, inject, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import type {KpiDatum} from '@cvhome-saas/ui-kit';
import {Money} from '@shared/i18n/money';
import {snapshot} from '@cvhome-saas/ui-kit';
import type {TrendPoint as ChartPoint} from '@shared/ui/charts/trend-chart';
import type {DateRangeValue} from '@shared/ui/date-range-picker/date-range-picker';
import {PlatformDashboardApi, type CompleteRange} from '../services/platform-dashboard.api.service';

/** How far back the dashboard looks when the page opens. */
const DEFAULT_RANGE_DAYS = 30;

const DAY_MS = 24 * 60 * 60 * 1000;

/** Minor units to major, as everything billing sends is in minor units. */
const MINOR_UNITS = 100;

/** The last 30 days, ending today. Computed rather than fixed, so it is right on any day. */
function defaultRange(): DateRangeValue {
  const to = new Date();
  return {from: new Date(to.getTime() - (DEFAULT_RANGE_DAYS - 1) * DAY_MS), to};
}

/**
 * The platform's own numbers, keyed on the chosen period.
 *
 * **Three series and money, where there were two counts.** Organizations and stores created per day
 * were the only platform-wide aggregates that existed; billing now answers subscriptions started per
 * day and revenue collected per day, so the third chart seller-ui always drew is real and the KPI row
 * has a figure on it that is not a signup count.
 *
 * **Money is one tile per currency, never a total.** Nothing on the platform holds an exchange rate,
 * so a combined figure would have to invent one. A period with no payments gets no revenue tile
 * rather than a zero: the row stays honest about what it knows.
 *
 * This page stays a summary. The reading of the same money per plan, per invoice and per event is
 * `/platform/billing`.
 */
@Injectable()
export class PlatformDashboardFacade {
  private readonly api = inject(PlatformDashboardApi);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);
  private readonly money = inject(Money);

  /** The requested period. Writing to it triggers a fetch. */
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

  private readonly loaded = snapshot(
    () => this.completeRange(),
    (range) => this.api.loadSnapshot(range),
  );

  readonly isLoading = this.loaded.isLoading;
  readonly error = this.loaded.error;
  readonly isEmpty = this.loaded.isEmpty;
  readonly reload = () => this.loaded.reload();

  readonly heading = computed(() => {
    this.transloco.activeLang();
    return {
      title: this.transloco.translate('platform.dashboard.heading.title'),
      context: this.transloco.translate('platform.dashboard.heading.context'),
    };
  });

  /**
   * The tiles, all of them over the chosen range.
   *
   * "New in this period", not "on the platform": the endpoints count creations between two dates and
   * a running total is a different query. The labels say which — a tile headed "Organizations" over a
   * 30-day count would be read as the platform's size.
   *
   * **The revenue tiles are one per currency, and there may be none.** They are appended rather than
   * fixed in place, because how many there are is a fact about the platform's markets rather than a
   * layout decision — and because a currency the platform stopped trading in should stop appearing.
   */
  readonly kpis = computed<readonly KpiDatum[]>(() => {
    this.transloco.activeLang();
    const value = this.loaded.value();
    const digits = (count: number) => this.localeFormat.localizeNumber(count, 'decimal');
    return [
      {
        label: this.transloco.translate('platform.dashboard.kpi.organizations'),
        value: value ? digits(value.organizationsTotal) : '—',
        icon: 'building',
        tone: 'green',
      },
      {
        label: this.transloco.translate('platform.dashboard.kpi.stores'),
        value: value ? digits(value.storesTotal) : '—',
        icon: 'shoppingCart',
        tone: 'cyan',
      },
      {
        label: this.transloco.translate('platform.dashboard.kpi.subscriptions'),
        value: value ? digits(value.subscriptionsTotal) : '—',
        icon: 'creditCard',
        tone: 'violet',
      },
      ...(value?.revenue ?? []).map((total) => ({
        label: this.transloco.translate('platform.dashboard.kpi.revenue', {
          currency: total.currency,
        }),
        // Minor units on the wire; the tile is money, so it is converted here and only here.
        value: this.money.account(total.minorUnits / MINOR_UNITS, total.currency),
        icon: 'dollar' as const,
        tone: 'green' as const,
      })),
    ];
  });

  /**
   * Subscriptions started per day, one chart per plan.
   *
   * One chart apiece rather than one stacked plot: `app-trend-chart` draws a single series, and the
   * catalogue is small enough that a row of small multiples reads better than a legend of plan codes
   * over a stack. The plan code is the series key, `UNKNOWN` included — a store whose plan id no
   * longer resolves is a visible bar rather than a total that quietly shrank.
   */
  readonly subscriptionSeries = computed<
    readonly {plan: string; total: string; points: readonly ChartPoint[]}[]
  >(() => {
    this.transloco.activeLang();
    return (this.loaded.value()?.subscriptions ?? []).map((series) => ({
      plan: series.plan,
      total: this.localeFormat.localizeNumber(series.total, 'decimal'),
      points: this.toChart(series.points),
    }));
  });

  /** Whether billing answered at all. False hides the chart row rather than drawing empty plots. */
  readonly hasSubscriptionSeries = computed(() => this.subscriptionSeries().length > 0);

  readonly organizations = computed<readonly ChartPoint[]>(() => this.toChart(this.loaded.value()?.organizations));
  readonly stores = computed<readonly ChartPoint[]>(() => this.toChart(this.loaded.value()?.stores));

  readonly organizationsTotal = computed(() =>
    this.localeFormat.localizeNumber(this.loaded.value()?.organizationsTotal ?? 0, 'decimal'),
  );
  readonly storesTotal = computed(() =>
    this.localeFormat.localizeNumber(this.loaded.value()?.storesTotal ?? 0, 'decimal'),
  );

  /**
   * A day series, with its axis labels in the reader's language.
   *
   * The ISO day stays as the track key so the label may change with the language without the chart
   * losing its identity for that point.
   */
  private toChart(points: readonly {date: string; value: number}[] | undefined): readonly ChartPoint[] {
    this.transloco.activeLang();
    return (points ?? []).map((point) => ({
      key: point.date,
      label: this.localeFormat.localizeDate(point.date, undefined, {month: 'short', day: 'numeric'}),
      value: point.value,
    }));
  }
}
