import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {NbCardModule, NbDatepickerModule, NbInputModule, NbSpinnerModule} from '@nebular/theme';
import {ClientDashboardFacade} from '../facades/client-dashboard.facade';
import {DashboardFilterFormService} from '../services/dashboard-filter-form.service';
import {DateRangeStateService} from '../state/date-range.state';
import {OrdersStatisticComponent} from '../charts/orders/orders-statistic.component';
import {CustomerCountriesStatisticComponent} from '../charts/customer-countries/customer-countries-statistic.component';
import {ProductsStatisticComponent} from '../charts/products/product-statistic.component';

@Component({
  selector: 'ngx-client-dashboard',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TranslateModule,
    NbCardModule,
    NbDatepickerModule,
    NbInputModule,
    NbSpinnerModule,
    OrdersStatisticComponent,
    CustomerCountriesStatisticComponent,
    ProductsStatisticComponent
  ],
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
