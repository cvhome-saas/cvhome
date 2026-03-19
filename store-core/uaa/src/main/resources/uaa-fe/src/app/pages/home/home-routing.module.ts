import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from './home.component';
import {SysUserGuard} from './shared/guards/sys-user.guard';
import {ManagedUserGuard} from './shared/guards/managed-user.guard';

const routes: Routes = [
  {
    path: '',
    component: HomeComponent,
    children: [
      {
        path: '',
        loadChildren: () => import('./sys/sys.module').then(it => it.SysModule),
        canMatch: [SysUserGuard]
      },
      {
        path: '',
        loadChildren: () => import('./managed/managed.module').then(it => it.ManagedModule),
        canMatch: [ManagedUserGuard]
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class HomeRoutingModule {
}
