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
      /*
       * A client is a page, not a pane. `ClientDetails` carries two URI arrays, two open key/value
       * maps and five groups of settings — Spring's own `ClientSettings` and `TokenSettings` — which
       * is more than a column beside a table can hold without becoming a form nobody can read.
       *
       * `new` is declared before `:id` because the router takes the first match, and a literal
       * segment that follows its own parameter is unreachable.
       */
      {
        path: 'clients/new',
        loadComponent: () => import('@features/client-form/client-form').then((m) => m.ClientForm),
        data: {titleKey: 'route.clientCreate'},
      },
      {
        path: 'clients/:id',
        loadComponent: () => import('@features/client-form/client-form').then((m) => m.ClientForm),
        data: {titleKey: 'route.clientEdit'},
      },
    ],
  },
  {path: '**', redirectTo: ''},
];
