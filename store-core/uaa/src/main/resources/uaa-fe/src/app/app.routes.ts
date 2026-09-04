import type {Routes} from '@angular/router';

import {canAccessSecuredPages} from '@cvhome-saas/ui-kit';

import {canAdministerRealm, isAdminLanding} from '@shared/auth/realm-admin';

/**
 * The split between these routes is Spring Security's, not this app's.
 *
 * `/login` is what `AppSecurityConfig.formLogin(loginPage("/login"))` points at and, with the logout
 * bounce and the two one-time-link pages, the only paths in here that are `permitAll`. Everything else
 * is `anyRequest().authenticated()`, and the admin API behind it additionally demands
 * `SCOPE_super_admin`/`ROLE_SUPER_ADMIN`.
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
    // Not under the shell: it paints one line and hands the browser to uaa's /logout.
    path: 'external-logout-link',
    loadComponent: () =>
      import('@features/external-logout-link/external-logout-link').then((m) => m.ExternalLogoutLink),
    data: {titleKey: 'route.signOut'},
  },
  /*
   * The two public pages a one-time link lands on. Outside the shell and anonymous: uaa permits the
   * paths and the `/api/v1/public/**` resources they call, and nothing else on the page needs a
   * session. `kind` picks the words and the endpoint; `?token=` arrives as a component input.
   */
  {
    path: 'accept-invitation',
    loadComponent: () => import('@cvhome-saas/ui-kit/uaa').then((m) => m.LinkAccept),
    data: {titleKey: 'route.acceptInvitation', kind: 'INVITATION', brand: 'cvhome identity'},
  },
  {
    path: 'reset-password',
    loadComponent: () => import('@cvhome-saas/ui-kit/uaa').then((m) => m.LinkAccept),
    data: {titleKey: 'route.resetPassword', kind: 'PASSWORD_RESET', brand: 'cvhome identity'},
  },
  {
    path: '',
    loadComponent: () => import('@layouts/admin-shell/admin-shell').then((m) => m.AdminShell),
    canActivate: [canAccessSecuredPages],
    children: [
      /*
       * Two empty-path routes, in order: an administrator matches the first and lands in the console,
       * everyone else falls through to the second and lands on their own account.
       */
      {path: '', pathMatch: 'full', canMatch: [isAdminLanding], redirectTo: 'users'},
      {path: '', pathMatch: 'full', redirectTo: 'account'},
      {
        path: 'users',
        canActivate: [canAdministerRealm],
        loadComponent: () => import('@features/users/users').then((m) => m.Users),
        data: {titleKey: 'route.users'},
      },
      {
        path: 'roles',
        canActivate: [canAdministerRealm],
        loadComponent: () => import('@features/roles/roles').then((m) => m.Roles),
        data: {titleKey: 'route.roles'},
      },
      {
        path: 'dashboard',
        canActivate: [canAdministerRealm],
        loadComponent: () => import('@features/dashboard/dashboard').then((m) => m.DashboardScreen),
        data: {titleKey: 'route.dashboard'},
      },
      {
        path: 'audit',
        canActivate: [canAdministerRealm],
        loadComponent: () => import('@features/audit/audit').then((m) => m.Audit),
        data: {titleKey: 'route.audit'},
      },
      {
        path: 'identity-providers',
        canActivate: [canAdministerRealm],
        loadComponent: () => import('@features/identity-providers/identity-providers').then((m) => m.IdentityProviders),
        data: {titleKey: 'route.providers'},
      },
      {
        path: 'account',
        loadComponent: () => import('@features/account/account').then((m) => m.Account),
        data: {titleKey: 'route.account'},
      },
      {
        path: 'settings',
        canActivate: [canAdministerRealm],
        loadComponent: () => import('@features/settings/settings').then((m) => m.Settings),
        data: {titleKey: 'route.settings'},
      },
      {
        path: 'clients',
        canActivate: [canAdministerRealm],
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
        canActivate: [canAdministerRealm],
        loadComponent: () => import('@features/client-form/client-form').then((m) => m.ClientForm),
        data: {titleKey: 'route.clientCreate'},
      },
      {
        path: 'clients/:id',
        canActivate: [canAdministerRealm],
        loadComponent: () => import('@features/client-form/client-form').then((m) => m.ClientForm),
        data: {titleKey: 'route.clientEdit'},
      },
    ],
  },
  {path: '**', redirectTo: ''},
];
