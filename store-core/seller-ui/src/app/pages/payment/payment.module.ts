import {NgModule} from '@angular/core';

import {PaymentComponent} from './payment.component';
import {PaymentRoutingModule} from './payment-routing.module';
import {PaymentListComponent} from './payment-list/payment-list.component';
import {PaymentApproveComponent} from './payment-approve/payment-approve';
import {NbDialogModule} from '@nebular/theme';
import {SharedModule} from "../shared/shared.module";

@NgModule({
  declarations: [
    PaymentComponent,
    PaymentListComponent,
    PaymentApproveComponent
  ],
  imports: [
    PaymentRoutingModule,
    SharedModule,
    NbDialogModule.forChild(),
  ]
})
export class PaymentModule {
}
