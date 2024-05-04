import {NgModule} from '@angular/core';

import {OrdersComponent} from './orders.component';
import {OrdersRoutingModule} from './orders-routing.module';
import {OrderListComponent} from './order-list/order-list.component';
import {OrderDetailsComponent} from './order-details/order-details.component';
import {OrderInvoiceComponent} from './order-invoice/order-invoice';
import {OrderHistoryComponent} from './order-history/order-history';
import {OrderTransactionComponent} from './order-transaction/order-transaction';
import {NbDialogModule} from '@nebular/theme';
import {SharedModule} from "../store-manager/shared/shared.module";

@NgModule({
  declarations: [
    OrdersComponent,
    OrderListComponent,
    OrderDetailsComponent,
    OrderInvoiceComponent,
    OrderHistoryComponent,
    OrderTransactionComponent
  ],
  imports: [
    OrdersRoutingModule,
    SharedModule,
    NbDialogModule.forChild(),
  ]
})
export class OrdersModule {
}
