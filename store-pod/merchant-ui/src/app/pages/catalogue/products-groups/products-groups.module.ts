import {NgModule} from '@angular/core';

import {ProductsGroupsRoutingModule} from './products-groups-routing.module';
import {ProductsGroupsComponent} from './products-groups.component';
import {GroupsListComponent} from './groups-list/groups-list.component';
import {ProductGroupFormComponent} from './product-group-form/product-group-form.component';
import {ActiveButtonComponent} from './groups-list/active-button.component';
import {SharedModule} from "../../../shared/shared.module";

@NgModule({
  declarations: [
    ProductsGroupsComponent,
    GroupsListComponent,
    ProductGroupFormComponent,
    ActiveButtonComponent,
  ],
  imports: [
    ProductsGroupsRoutingModule,
    SharedModule,
  ]
})
export class ProductsGroupsModule {
}
