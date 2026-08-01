import {Routes} from '@angular/router';
import {GroupsListComponent} from './groups-list/groups-list.component';
import {ProductsGroupsComponent} from './products-groups.component';
import {ProductGroupFormComponent} from './product-group-form/product-group-form.component';

export const PRODUCTS_GROUPS_ROUTES: Routes = [
  {
    path: '',
    component: ProductsGroupsComponent,
    children: [
      {
        path: 'update-products-group/:code',
        component: ProductGroupFormComponent,
      },
      {
        path: 'create-products-group',
        component: ProductGroupFormComponent,
      },
      {
        path: 'groups-list',
        component: GroupsListComponent,
      },
    ],
  }
];
