import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { OrdersComponent } from './orders.component';
import { OrderListComponent } from './order-list/order-list.component';
import { OrderDetailsComponent } from './order-details/order-details.component';


const routes: Routes = [
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


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class OrdersRoutingModule {
}
