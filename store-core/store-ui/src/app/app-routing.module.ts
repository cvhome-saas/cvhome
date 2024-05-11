import {ExtraOptions, RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';
import {environment} from "../environments/environment";
import {canAccessWelcomePage} from "./shared/service/auth-guard.service";

export const routes: Routes = [
  {
    path: 'pages',
    loadChildren: () => import('./pages/pages.module')
      .then(m => m.PagesModule),
  },
  {
    path: 'external-login-link',
    loadChildren: () => new Promise(() => {
      window.location.href = environment.LOGIN_URL;
    })
  },
  {
    path: 'external-logout-link',
    loadChildren: () => new Promise(() => {
      window.location.href = environment.LOGOUT_URL;
    })
  },
  {
    path: '',
    canActivate:[canAccessWelcomePage],
    loadChildren: () => import('./welcome/welcome.module')
      .then(m => m.WelcomeModule),
  },
  {path: '**', redirectTo: ''},
];

const config: ExtraOptions = {
  useHash: false,
};

@NgModule({
  imports: [RouterModule.forRoot(routes, config)],
  exports: [RouterModule],
})
export class AppRoutingModule {
}
