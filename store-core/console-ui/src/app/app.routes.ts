import { Routes } from '@angular/router';

import {firstRunOnly, requiresStore} from '@layouts/console-shell/guards/first-run.guard';

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
    ],
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
    canActivate: [firstRunOnly],
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
    canActivate: [requiresStore],
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
    canActivate: [requiresStore],
    children: [
      {
        path: '',
        loadComponent: () => import('@features/orders/orders').then((page) => page.Orders),
        data: {titleKey: 'route.orders.title', breadcrumbKey: 'shell.breadcrumb.orders'},
      },
    ],
  },
  {
    path: 'store-management',
    loadComponent: () => import('@layouts/console-shell/console-shell').then((layout) => layout.ConsoleShell),
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
