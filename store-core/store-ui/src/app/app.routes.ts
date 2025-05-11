import {Routes} from '@angular/router';
import {environment} from '../environments/environment';

export const routes: Routes = [
  {
    path: 'public',
    loadChildren: () => import('./public/public.module')
      .then(m => m.PublicModule),
  },
  {
    path: 'pages',
    loadChildren: () => import('./pages/pages.module')
      .then(m => m.PagesModule),
  },
  {
    path: 'external-login-link',
    loadChildren: () => new Promise(() => {
      window.location.href = environment.LOGIN_URL+"?redirectTo="+encodeURIComponent(window.location.pathname);
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
