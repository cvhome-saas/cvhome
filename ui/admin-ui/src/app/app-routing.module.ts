import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {inGuard, outGuard} from "./service/auth-guard.service";

const routes: Routes = [
  {
    path: 'in',
    canActivate: [inGuard()],
    loadChildren: () => import('./views/in/in.module').then(m => m.InModule)
  },
  {
    path: '',
    canActivate: [outGuard()],
    loadChildren: () => import('./views/out/out.module').then(m => m.OutModule)
  },
];

// configures NgModule imports and exports
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
