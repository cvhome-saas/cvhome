import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {CustomersComponent} from './components/customer.component';
import {CustomerListComponent} from './components/customer-list.component';
const routes: Routes = [{
  path: '',
  component: CustomersComponent,
  children: [
    {
      path: 'list',
      component: CustomerListComponent,
    },
  ],
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class CustomersRoutingModule {
}

export const routedComponents = [
  CustomersComponent,
  CustomerListComponent
];
