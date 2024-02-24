import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {inGuard} from "./service/auth-guard.service";
import {Environment} from "@angular/cli/lib/config/workspace-schema";
import {environment} from "../environment";

const routes: Routes = [
  {
    path: '',
    canActivate: [inGuard()],
    loadChildren: () => import('./views/in/in.module').then(m => m.InModule)
  },
  {
    path: '',
    loadChildren: () => import('./views/out/out.module').then(m => m.OutModule)
  },
  {
    path: 'external-login-link',
    loadChildren: () => new Promise( () => {  window.location.href = environment.LOGIN_URL; } )
  }
];

// configures NgModule imports and exports
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
