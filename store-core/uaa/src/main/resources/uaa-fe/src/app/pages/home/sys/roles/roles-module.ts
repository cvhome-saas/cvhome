import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RolesRoutingModule} from './roles-routing.module';
import {SharedModule} from '../../shared/shared.module';
import {RoleFormComponent} from './role-form-component/role-form-component';
import {RoleCreateComponent} from './role-create-component/role-create-component';
import {RoleEditComponent} from './role-edit-component/role-edit-component';
import {RolesListComponent} from './role-list-component/roles-list-component';


@NgModule({
  declarations: [
    RolesListComponent,
    RoleFormComponent,
    RoleEditComponent,
    RoleCreateComponent,
    RoleFormComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    RolesRoutingModule,
  ]
})
export class RolesModule {
}
