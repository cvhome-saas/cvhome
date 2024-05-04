import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TypesRoutingModule} from './types-routing.module';
import {TypesListComponent} from './types-list/types-list.component';
import {TypesComponent} from './types.component';
import {TypeDetailsComponent} from './type-details/type-details.component';
import {SharedModule} from "../../store-manager/shared/shared.module";

@NgModule({
  declarations: [TypesComponent, TypesListComponent, TypeDetailsComponent],
  imports: [
    CommonModule,
    TypesRoutingModule,
    SharedModule,
  ]
})
export class TypesModule {
}
