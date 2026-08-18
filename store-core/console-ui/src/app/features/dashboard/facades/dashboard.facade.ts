import {computed, inject, Injectable, linkedSignal, signal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import type {ActionItem} from '@shared/ui/action-list/action-list';
import type {DonutSlice} from '@shared/ui/charts/donut-chart';
import type {KpiDatum} from '@shared/ui/kpi-card/kpi-card';
import type {DashboardSnapshot} from '@models/dashboard';
import type {RankedItem} from '@shared/ui/ranked-list/ranked-list';
import type {DateRangeValue} from '@shared/ui/date-range-picker/date-range-picker';
import {DashboardApi} from '../services/dashboard.api.service';

const DEFAULT_RANGE: DateRangeValue = {from: new Date(2026, 6, 5), to: new Date(2026, 7, 4)};

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
  private readonly localeFormat = inject(TranslocoLocaleService);

  /** Fixture "now" for the heading's context line — see `heading` below. */
  private static readonly HEADING_DATE = new Date(2026, 7, 4);

  /**
   * `computed()` rather than a static object: it renders once per page load but must still
   * follow a language switch, and a plain field would freeze at whatever language was
   * active when this root-singleton facade was built.
   */
  readonly heading = computed(() => {
    this.transloco.activeLang();
    return {
      title: this.transloco.translate('dashboard.heading.title'),
      // "Acme Supply Co." is the open store's name — real data, not UI copy — but the
      // date follows the active locale like everything else.
      context: this.transloco.translate('dashboard.heading.context', {
        store: 'Acme Supply Co.',
        date: this.localeFormat.localizeDate(DashboardFacade.HEADING_DATE, undefined, {dateStyle: 'medium'}),
      }),
    };
  });

  /** The requested period. Writing to it triggers a fetch. */
  readonly dateRange = signal<DateRangeValue>(DEFAULT_RANGE);

  private readonly snapshot = rxResource({
    params: () => this.dateRange(),
    stream: ({params}) => this.api.loadSnapshot(params),
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
      value: kpi.value,
      icon: kpi.icon,
      tone: kpi.tone,
      delta: kpi.delta,
      flag: kpi.flagKey ? this.transloco.translate(kpi.flagKey) : undefined,
    }));
  });

  readonly attention = computed<readonly ActionItem[]>(() => {
    this.transloco.activeLang();
    return (this.loaded()?.attention ?? []).map((item) => ({
      label: this.transloco.translate(item.labelKey),
      detail: this.transloco.translate(item.detailKey),
      count: item.count,
      icon: item.icon,
      tone: item.tone,
    }));
  });

  readonly orderStatuses = computed(() => {
    this.transloco.activeLang();
    return (this.loaded()?.orderStatuses ?? []).map((status) => ({
      label: this.transloco.translate(status.labelKey),
      value: status.value,
      tone: status.tone,
    }));
  });

  readonly customerSplit = computed<readonly DonutSlice[]>(() => {
    this.transloco.activeLang();
    return (this.loaded()?.customerSplit ?? []).map((segment) => ({
      label: this.transloco.translate(segment.labelKey),
      value: segment.value,
      tone: segment.tone,
    }));
  });

  /** Product names are catalog data — a real API would already return them localized. */
  readonly topProducts = computed<readonly RankedItem[]>(() =>
    (this.loaded()?.products ?? []).map((product) => ({
      label: product.name,
      value: product.sales,
    })),
  );

  retry(): void {
    this.snapshot.reload();
  }
}
