import {Component, OnInit, inject} from '@angular/core';
import {AdminDashboardFacade} from '../facades/admin-dashboard.facade';
import {DashboardFilterFormService} from '../services/dashboard-filter-form.service';
import {DateRangeStateService} from '../state/date-range.state';

@Component({
  selector: 'ngx-admin-dashboard',
  standalone: false,
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss',
  providers: [AdminDashboardFacade, DateRangeStateService, DashboardFilterFormService]
})
export class AdminDashboardComponent implements OnInit {
  protected readonly facade = inject(AdminDashboardFacade);
  protected readonly formService = inject(DashboardFilterFormService);

  ngOnInit(): void {
    this.facade.init();
  }

  onFromDateChanged(date: Date): void {
    this.facade.onFromDateChange(date);
  }

  onToDateChanged(date: Date): void {
    this.facade.onToDateChange(date);
  }
}
