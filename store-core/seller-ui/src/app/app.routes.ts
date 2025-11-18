import {Routes} from '@angular/router';
import {ExternalLoginLinkComponent} from "./public/routing/external-login-link.component";
import {ExternalLogoutLinkComponent} from "./public/routing/external-logout-link.component";

export const routes: Routes = [
  {
    path: 'pages',
    loadChildren: () => import('./pages/pages.module')
      .then(m => m.PagesModule),
  },
  {
    path: '',
    loadChildren: () => import('./public/public.module')
      .then(m => m.PublicModule),
  },
  {
    path: 'external-login-link',
    component: ExternalLoginLinkComponent,
  },
  {
    path: 'external-logout-link',
    component: ExternalLogoutLinkComponent,
  },
];
