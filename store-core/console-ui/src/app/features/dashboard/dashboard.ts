import {Component, ElementRef, computed, inject, viewChild} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import {ActionList} from '@shared/ui/action-list/action-list';
import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {BarChart} from '@shared/ui/charts/bar-chart';
import {DonutChart} from '@shared/ui/charts/donut-chart';
import {ExportButton} from '@shared/ui/export-button/export-button';
import {DateRangePicker} from '@shared/ui/date-range-picker/date-range-picker';
import {Icon} from '@shared/ui/icon/icon';
import {LoadError} from '@shared/ui/load-error/load-error';
import {KpiGrid} from '@shared/ui/kpi-grid/kpi-grid';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {RankedList} from '@shared/ui/ranked-list/ranked-list';
import {DashboardFacade} from './facades/dashboard.facade';

/**
 * The console's home page.
 *
 * It renders into `ConsoleShell`, which owns the banner, navigation rail and toolbar, so
 * this component is only its own content — a page header and its widgets.
 *
 * Picking a reporting period is a data request: the facade refetches, the widgets go
 * under a loading veil, and every figure comes back from the response.
 */
@Component({
  selector: 'app-dashboard',
  imports: [
    LoadError,
    ActionList,
    BarChart,
    BusyOverlay,
    DateRangePicker,
    DonutChart,
    ExportButton,
    Icon,
    KpiGrid,
    PageHeader,
    RankedList,
    TranslocoDirective,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  protected readonly facade = inject(DashboardFacade);
  private readonly localeFormat = inject(TranslocoLocaleService);

  protected readonly heading = this.facade.heading;
  protected readonly dateRange = this.facade.dateRange;

  protected readonly isLoading = this.facade.isLoading;
  protected readonly isEmpty = this.facade.isEmpty;
  protected readonly error = this.facade.error;

  protected readonly kpis = this.facade.kpis;
  protected readonly attention = this.facade.attention;
  protected readonly orderStatuses = this.facade.orderStatuses;
  protected readonly customerSplit = this.facade.customerSplit;
  protected readonly topProducts = this.facade.topProducts;

  /** The region the export captures. Absent until the first response renders it. */
  // Explicitly the element — see the note in `products.ts`.
  protected readonly widgets = viewChild('widgets', {read: ElementRef});

  /** Names the period on the exported PDF's first page. */
  protected readonly exportSubtitle = computed(() => {
    const {from, to} = this.dateRange();
    if (!from || !to) {
      return this.heading().context;
    }
    const format = (date: Date) => this.localeFormat.localizeDate(date, undefined, {dateStyle: 'medium'});
    return `${format(from)} – ${format(to)}`;
  });
}
