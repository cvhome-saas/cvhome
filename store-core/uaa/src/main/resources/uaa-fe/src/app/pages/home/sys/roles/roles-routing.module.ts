import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {RolesComponent} from './roles.component';
import {RolesListComponent} from './role-list-component/roles-list-component';
import {RoleCreateComponent} from './role-create-component/role-create-component';
import {RoleEditComponent} from './role-edit-component/role-edit-component';

const routes: Routes = [
  {
    path: '',
    component: RolesComponent,
    children: [
      {
        path: '',
        component: RolesListComponent
      },
      {
        path: 'create',
        component: RoleCreateComponent
      },
      {
        path: 'edit/:roleId',
        component: RoleEditComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RolesRoutingModule {
}
