import {Routes} from '@angular/router';

export const routes: Routes = [
  {
    path: 'pages',
    loadChildren: () => import('./pages/pages.module')
      .then(m => m.PagesModule),
  },
  {
    path: '',
    loadChildren: () => import('./public/public.routes')
      .then(m => m.PUBLIC_ROUTES),
  }
];
