import {Component, OnInit, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {NbCardModule, NbDatepickerModule, NbInputModule, NbSpinnerModule} from '@nebular/theme';
import {AdminDashboardFacade} from '../facades/admin-dashboard.facade';
import {DashboardFilterFormService} from '../services/dashboard-filter-form.service';
import {DateRangeStateService} from '../state/date-range.state';
import {SubscriptionStatisticComponent} from '../charts/subscription/subscription-statistic.component';
import {NewOrgJoinerStatisticComponent} from '../charts/new-org-joiner/new-org-joiner-statistic.component';
import {NewStoreCreatedStatisticComponent} from '../charts/new-store-created/new-store-created-statistic.component';

@Component({
  selector: 'ngx-admin-dashboard',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TranslateModule,
    NbCardModule,
    NbDatepickerModule,
    NbInputModule,
    NbSpinnerModule,
    SubscriptionStatisticComponent,
    NewOrgJoinerStatisticComponent,
    NewStoreCreatedStatisticComponent
  ],
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
