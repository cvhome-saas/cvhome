import {Routes} from '@angular/router';
import {PaymentComponent} from './payment.component';
import {PaymentListComponent} from './components/payment-list.component';

export const PAYMENT_ROUTES: Routes = [
  {
    path: '',
    component: PaymentComponent,
    children: [
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
