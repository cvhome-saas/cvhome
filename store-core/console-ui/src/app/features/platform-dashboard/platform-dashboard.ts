import {Component, inject} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {TrendChart} from '@shared/ui/charts/trend-chart';
import {DateRangePicker} from '@shared/ui/date-range-picker/date-range-picker';
import {KpiGrid} from '@shared/ui/kpi-grid/kpi-grid';
import {LoadError} from '@shared/ui/load-error/load-error';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {PlatformDashboardFacade} from './facades/platform-dashboard.facade';

/**
 * The platform's own numbers — the operator's home, and the first page a super admin lands on.
 *
 * Two series and two totals, which is everything the platform can count. seller-ui's admin home drew
 * a third chart from `subscription-statistic`, an endpoint that exists in no Java file; that panel is
 * absent here rather than empty, because an empty card claims there is nothing to show.
 */
@Component({
  selector: 'app-platform-dashboard',
  imports: [
    BusyOverlay,
    DateRangePicker,
    KpiGrid,
    LoadError,
    NoticeBar,
    PageHeader,
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
