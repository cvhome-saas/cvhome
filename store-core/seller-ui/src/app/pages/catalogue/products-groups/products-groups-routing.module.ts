import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {GroupsListComponent} from './groups-list/groups-list.component';
import {ProductsGroupsComponent} from './products-groups.component';
import {ProductGroupFormComponent} from './product-group-form/product-group-form.component';

const routes: Routes = [
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

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProductsGroupsRoutingModule {
}
