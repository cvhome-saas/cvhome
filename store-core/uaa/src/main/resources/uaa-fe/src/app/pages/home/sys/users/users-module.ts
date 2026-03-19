import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UsersRoutingModule} from './users-routing.module';
import {SharedModule} from '../../shared/shared.module';
import {UsersListComponent} from './users-list-component/users-list-component';
import {UserEditComponent} from './user-edit-component/user-edit-component';
import {UserFormComponent} from './user-form-component/user-form-component';
import {UserCreateComponent} from './user-create-component/user-create-component';


@NgModule({
  declarations: [
    UsersListComponent,
    UserFormComponent,
    UserEditComponent,
    UserCreateComponent,
    UserFormComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    UsersRoutingModule,
  ]
})
export class UsersModule {
}
