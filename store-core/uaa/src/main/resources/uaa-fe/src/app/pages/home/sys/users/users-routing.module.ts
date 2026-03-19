import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {UsersComponent} from './users.component';
import {UsersListComponent} from './users-list-component/users-list-component';
import {UserCreateComponent} from './user-create-component/user-create-component';
import {UserEditComponent} from './user-edit-component/user-edit-component';

const routes: Routes = [
  {
    path: '',
    component: UsersComponent,
    children: [
      {
        path: '',
        component: UsersListComponent
      },
      {
        path: 'create',
        component: UserCreateComponent
      },
      {
        path: 'edit/:userId',
        component: UserEditComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UsersRoutingModule {
}
