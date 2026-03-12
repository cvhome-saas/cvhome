import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'clients',
    pathMatch: 'full'
  },
  {
    path: 'clients',
    loadChildren: () => import('./clients/clients-module').then(it => it.ClientsModule)
  },
  {
    path: 'users',
    loadChildren: () => import('./users/users-module').then(it => it.UsersModule)
  },
  {
    path: 'roles',
    loadChildren: () => import('./roles/roles-module').then(it => it.RolesModule)
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SysRoutingModule { }
