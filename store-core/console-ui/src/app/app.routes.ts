import { Routes } from '@angular/router';

import {canAccessSecuredPages} from '@core/auth/auth-guard.service';
import {consoleContext, firstRunOnly, requiresStore} from '@layouts/console-shell/guards/first-run.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('@layouts/marketing-shell/marketing-shell').then((layout) => layout.MarketingShell),
    children: [
      {
        path: '',
        loadComponent: () => import('@features/marketing/marketing').then((page) => page.Marketing),
        data: {titleKey: 'route.home.title'},
      },
      {
        path: 'terms',
        loadComponent: () => import('@features/legal/legal-page').then((page) => page.LegalPage),
        data: {titleKey: 'route.terms.title', document: 'terms'},
      },
      {
        path: 'privacy-policy',
        loadComponent: () => import('@features/legal/legal-page').then((page) => page.LegalPage),
        data: {titleKey: 'route.privacyPolicy.title', document: 'privacy'},
      },
      {
        // Where the hosted checkout returns to. Client-rendered: nothing here is worth prerendering, and both
        // outcomes are reachable by URL alone.
        path: 'subscription/success',
        loadComponent: () =>
          import('@features/subscription/subscription-outcome').then((page) => page.SubscriptionOutcome),
        data: {titleKey: 'route.subscriptionSuccess.title', succeeded: true},
      },
      {
        path: 'subscription/fail',
        loadComponent: () =>
          import('@features/subscription/subscription-outcome').then((page) => page.SubscriptionOutcome),
        data: {titleKey: 'route.subscriptionFail.title', succeeded: false},
      },
    ],
  },
  {
    // Not under a shell: it paints one line and immediately hands the browser to the gateway.
    path: 'external-logout-link',
    loadComponent: () =>
      import('@features/auth/external-logout-link/external-logout-link').then((page) => page.ExternalLogoutLink),
    data: {titleKey: 'route.signOut.title'},
  },
  {
    path: 'sign-in',
    loadComponent: () => import('@layouts/auth-shell/auth-shell').then((layout) => layout.AuthShell),
    children: [
      {
        path: '',
        loadComponent: () => import('@features/auth/sign-in/sign-in').then((page) => page.SignIn),
        data: {titleKey: 'route.signIn.title'},
      },
    ],
  },
  {
    path: 'sign-up',
    loadComponent: () => import('@layouts/auth-shell/auth-shell').then((layout) => layout.AuthShell),
    children: [
      {
        path: '',
        loadComponent: () => import('@features/auth/sign-up/sign-up').then((page) => page.SignUp),
        data: {titleKey: 'route.signUp.title'},
      },
    ],
  },
  {
    // Where an account with no store is held: the rail is disabled and creating a store is
    // the only way on. `firstRunOnly` hands over to the console once one exists.
    path: 'getting-started',
    loadComponent: () => import('@layouts/console-shell/console-shell').then((layout) => layout.ConsoleShell),
    canActivate: [canAccessSecuredPages, consoleContext, firstRunOnly],
    children: [
      {
        path: '',
        loadComponent: () => import('@features/first-run/first-run').then((page) => page.FirstRun),
        data: {titleKey: 'route.firstRun.title', breadcrumbKey: 'shell.breadcrumb.firstRun'},
      },
    ],
  },
  {
    path: 'dashboard',
    loadComponent: () => import('@layouts/console-shell/console-shell').then((layout) => layout.ConsoleShell),
    canActivate: [canAccessSecuredPages, consoleContext, requiresStore],
    children: [
      {
        path: '',
        loadComponent: () => import('@features/dashboard/dashboard').then((page) => page.Dashboard),
        // Read by the console toolbar's breadcrumb; every console page supplies one.
        data: {titleKey: 'route.dashboard.title', breadcrumbKey: 'shell.breadcrumb.dashboard'},
      },
    ],
  },
  {
    path: 'orders',
    loadComponent: () => import('@layouts/console-shell/console-shell').then((layout) => layout.ConsoleShell),
    canActivate: [canAccessSecuredPages, consoleContext, requiresStore],
    children: [
      {
        path: '',
        loadComponent: () => import('@features/orders/orders').then((page) => page.Orders),
        data: {titleKey: 'route.orders.title', breadcrumbKey: 'shell.breadcrumb.orders'},
      },
      {
        // `withComponentInputBinding()` binds `:id` straight onto the component's `id` input, so
        // navigating from one order to another re-reads without recreating the page.
        path: ':id',
        loadComponent: () =>
          import('@features/order-details/order-details').then((page) => page.OrderDetails),
        data: {titleKey: 'route.orderDetails.title', breadcrumbKey: 'shell.breadcrumb.orderDetails'},
      },
    ],
  },
  {
    path: 'store-management',
    loadComponent: () => import('@layouts/console-shell/console-shell').then((layout) => layout.ConsoleShell),
    // Authentication and the store list for the whole branch; a *store* is not required, because
    // `create` below is the only way out of first run — but the rail still renders on it, so the list
    // has to load there too.
    canActivate: [canAccessSecuredPages, consoleContext],
    children: [
      // Static before the section param, so this does not get swallowed as a section name.
      {
        path: 'create',
        loadComponent: () => import('@features/create-store/create-store').then((page) => page.CreateStore),
        data: {titleKey: 'route.createStore.title', breadcrumbKey: 'shell.breadcrumb.createStore'},
      },
      // The section is part of the URL, so a settings card is linkable and survives a reload.
      {path: '', redirectTo: 'branding', pathMatch: 'full'},
      {
        // Guarded here rather than on the parent so `create` above stays reachable — it is
        // the only exit from first run.
        path: ':section',
        loadComponent: () => import('@features/store-management/store-management').then((page) => page.StoreManagement),
        // Authentication is already asserted by the parent; this adds only the store requirement.
        canActivate: [requiresStore],
        data: {titleKey: 'route.storeManagement.title', breadcrumbKey: 'shell.breadcrumb.storeManagement'},
      },
    ],
  },
  {
    path: '**',
    loadComponent: () => import('@features/not-found/not-found').then((page) => page.NotFound),
    data: {titleKey: 'route.notFound.title'},
  },
];
