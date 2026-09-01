import type {Routes} from '@angular/router';

import {canAccessSecuredPages} from '@cvhome-saas/ui-kit';

/**
 * Four routes, and the split between them is Spring Security's, not this app's.
 *
 * `/login` is what `AppSecurityConfig.formLogin(loginPage("/login"))` points at and the only path in
 * here that is `permitAll`. Everything else is `anyRequest().authenticated()`, and the admin API
 * behind it additionally demands `SCOPE_super_admin`/`ROLE_SUPER_ADMIN`.
 *
 * `StaticController` forwards any path without a dot that is not `/api/` or `/oauth2/` to
 * index.html, so a deep link to `/clients` reaches the router rather than 404ing.
 */
export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('@features/sign-in/sign-in').then((m) => m.SignIn),
    data: {titleKey: 'route.signIn'},
  },
  {
    path: '',
    loadComponent: () => import('@layouts/admin-shell/admin-shell').then((m) => m.AdminShell),
    canActivate: [canAccessSecuredPages],
    children: [
      {path: '', pathMatch: 'full', redirectTo: 'users'},
      {
        path: 'users',
        loadComponent: () => import('@features/users/users').then((m) => m.Users),
        data: {titleKey: 'route.users'},
      },
      {
        path: 'roles',
        loadComponent: () => import('@features/roles/roles').then((m) => m.Roles),
        data: {titleKey: 'route.roles'},
      },
      {
        path: 'clients',
        loadComponent: () => import('@features/clients/clients').then((m) => m.Clients),
        data: {titleKey: 'route.clients'},
      },
    ],
  },
  {path: '**', redirectTo: ''},
];
