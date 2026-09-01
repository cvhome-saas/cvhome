import {Component, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import {BusyOverlay, DateRangePicker, KpiGrid, LoadError, PageHeader} from '@cvhome-saas/ui-kit/ui';
import {TrendChart} from '@shared/ui/charts/trend-chart';
import {PlatformDashboardFacade} from './facades/platform-dashboard.facade';

/**
 * The platform's own numbers — the operator's home, and the first page a super admin lands on.
 *
 * Three series and money. seller-ui's admin home drew a third chart from `subscription-statistic`,
 * an endpoint that existed in no Java file and had been a 404 for its entire life; it exists now, so
 * the chart is drawn — one plot per plan — and the KPI row carries revenue per currency beside the
 * two signup counts.
 *
 * It stays a summary. The reading of the same money per plan, per invoice and per audit event is
 * `/platform/billing`, which this page links to rather than reproducing.
 */
@Component({
  selector: 'app-platform-dashboard',
  imports: [
    BusyOverlay,
    DateRangePicker,
    KpiGrid,
    LoadError,
    PageHeader,
    RouterLink,
    TranslocoDirective,
    TrendChart,
  ],
  providers: [PlatformDashboardFacade],
  templateUrl: './platform-dashboard.html',
  styleUrl: './platform-dashboard.css',
})
export class PlatformDashboard {
  protected readonly facade = inject(PlatformDashboardFacade);
}
