import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {ClientDashboardFacade} from '../facades/client-dashboard.facade';
import {DashboardFilterFormService} from '../services/dashboard-filter-form.service';
import {DateRangeStateService} from '../state/date-range.state';

@Component({
  selector: 'ngx-client-dashboard',
  standalone: false,
  templateUrl: './client-dashboard.component.html',
  styleUrl: './client-dashboard.component.scss',
  providers: [ClientDashboardFacade, DateRangeStateService, DashboardFilterFormService]
})
export class ClientDashboardComponent implements OnInit {
  protected readonly facade = inject(ClientDashboardFacade);
  protected readonly formService = inject(DashboardFilterFormService);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }

  onFromDateChanged(date: Date): void {
    this.facade.onFromDateChange(date);
  }

  onToDateChanged(date: Date): void {
    this.facade.onToDateChange(date);
  }
}
