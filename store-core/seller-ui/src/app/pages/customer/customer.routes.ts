import {Routes} from '@angular/router';
import {CustomersComponent} from './components/customer.component';
import {CustomerListComponent} from './components/customer-list.component';

export const CUSTOMER_ROUTES: Routes = [{
  path: '',
  component: CustomersComponent,
  children: [
    {
      path: 'list',
      component: CustomerListComponent,
    },
  ],
}];
