import {Routes} from '@angular/router';
import {OrdersComponent} from './orders.component';
import {OrderListComponent} from './components/order-list.component';
import {OrderDetailsComponent} from './order-details/order-details.component';

export const ORDERS_ROUTES: Routes = [
  {
    path: '', component: OrdersComponent, children: [
      {
        path: '',
        redirectTo: 'order-list',
        pathMatch: 'full',
      },
      {
        path: 'order-list',
        component: OrderListComponent,
      },
      {
        path: 'order-details/:id',
        component: OrderDetailsComponent,
      }
    ],
  }
];
