import {ExtraOptions, RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';
import {environment} from "../environments/environment";

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
  {path: '', redirectTo: '/pages', pathMatch: "full"},
  {path: '**', redirectTo: '/pages', pathMatch: "full"},
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
