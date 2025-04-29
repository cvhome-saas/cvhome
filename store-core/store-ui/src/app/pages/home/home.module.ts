import {NgModule} from '@angular/core';

import {HomeComponent} from './home.component';
import {HomeRoutingModule} from "./home-routing.module";
import {NgxEchartsModule} from 'ngx-echarts';
import {CustomerCountriesStatisticComponent} from "./charts/customer-countries/customer-countries-statistic.component";
import {OrdersStatisticComponent} from "./charts/orders/orders-statistic.component";
import {ProductsStatisticComponent} from "./charts/products/product-statistic.component";
import {ClientDashboardComponent} from "./client-dashboard/client-dashboard.component";
import {AdminDashboardComponent} from "./admin-dashboard/admin-dashboard.component";
import {NewOrgJoinerStatisticComponent} from "./charts/new-org-joiner/new-org-joiner-statistic.component";
import {NewStoreCreatedStatisticComponent} from "./charts/new-store-created/new-store-created-statistic.component";
import {SubscriptionStatisticComponent} from "./charts/subscription/subscription-statistic.component";
import {SharedModule} from '../../shared/shared.module';

@NgModule({
  declarations: [
    CustomerCountriesStatisticComponent,
    OrdersStatisticComponent,
    ProductsStatisticComponent,
    NewOrgJoinerStatisticComponent,
    NewStoreCreatedStatisticComponent,
    HomeComponent,
    ClientDashboardComponent,
    SubscriptionStatisticComponent,
    AdminDashboardComponent
  ],
  imports: [
    SharedModule,
    NgxEchartsModule.forRoot({
      echarts: () => import('echarts'), // or import('./path-to-my-custom-echarts')
    }),
    HomeRoutingModule
  ]
})
export class HomeModule {
}
