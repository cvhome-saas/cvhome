import {Routes} from '@angular/router';
import {UserManagementComponent} from './user-management.component';
import {UserProfileComponent} from './user-profile/user-profile.component';
import {UsersListComponent} from './users-list/users-list.component';
import {ChangePasswordComponent} from './change-password/change-password.component';
import {CreateNewUserComponent} from './create-new-user/create-new-user.component';
import {UserDetailsComponent} from './user-details/user-details.component';

export const USER_MANAGEMENT_ROUTES: Routes = [
  {
    path: '', component: UserManagementComponent, children: [
      {
        path: '',
        redirectTo: 'profile',
        pathMatch: 'full',
      },
      {
        path: 'profile',
        component: UserProfileComponent,
      },
      {
        path: 'change-password/:id',
        component: ChangePasswordComponent,
      },
      {
        path: 'create-user/:store',
        component: CreateNewUserComponent,
      },
      {
        path: 'users',
        component: UsersListComponent,
      },
      {
        path: 'user/:id',
        component: UserDetailsComponent,
      },
    ],
  }
];
