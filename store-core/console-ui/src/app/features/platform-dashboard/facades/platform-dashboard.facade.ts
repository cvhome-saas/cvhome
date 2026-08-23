import {Injectable, computed, inject, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import type {KpiDatum} from '@models/ui';
import {snapshot} from '@shared/state/snapshot';
import type {TrendPoint as ChartPoint} from '@shared/ui/charts/trend-chart';
import type {DateRangeValue} from '@shared/ui/date-range-picker/date-range-picker';
import {PlatformDashboardApi, type CompleteRange} from '../services/platform-dashboard.api.service';

/** How far back the dashboard looks when the page opens. */
const DEFAULT_RANGE_DAYS = 30;

const DAY_MS = 24 * 60 * 60 * 1000;

/** The last 30 days, ending today. Computed rather than fixed, so it is right on any day. */
function defaultRange(): DateRangeValue {
  const to = new Date();
  return {from: new Date(to.getTime() - (DEFAULT_RANGE_DAYS - 1) * DAY_MS), to};
}

/**
 * The platform's own numbers, keyed on the chosen period.
 *
 * **Two counts and nothing else, on purpose.** Organizations created per day and stores created per
 * day are the only platform-wide aggregates that exist. There is no revenue figure anywhere on the
 * platform, no subscription statistic (the endpoint is in no Java file), and no per-pod or per-plan
 * breakdown. A KPI row of four tiles, two of them permanently em dashes, would be a worse page than
 * a row of two that are real.
 */
@Injectable()
export class PlatformDashboardFacade {
  private readonly api = inject(PlatformDashboardApi);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);

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
   * Two tiles, both counts over the chosen range.
   *
   * "New in this period", not "on the platform": the endpoints count creations between two dates and
   * a running total is a different query that does not exist. The labels say which — a tile headed
   * "Organizations" over a 30-day count would be read as the platform's size.
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
    ];
  });

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
