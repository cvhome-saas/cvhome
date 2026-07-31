import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {PaymentComponent} from './payment.component';
import {PaymentListComponent} from './payment-list/payment-list.component';


const routes: Routes = [
  {
    path: '', component: PaymentComponent, children: [
      {
        path: '',
        redirectTo: 'payment-list',
        pathMatch: 'full',
      },
      {
        path: 'payment-list',
        component: PaymentListComponent,
      }
    ],
  }
];


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PaymentRoutingModule {
}
