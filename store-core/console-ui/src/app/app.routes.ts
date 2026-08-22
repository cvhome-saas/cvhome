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
        /*
         * Where the hosted checkout returns to, and the path is not negotiable: `SubscriptionApi`
         * builds it from `SUCCESS_PATH = "/public/subscription/success"`, so `public` is a real URL
         * segment here even though nothing else in this app uses one.
         *
         * It had been mounted at `subscription/success` — no `public` — which matched nothing, so a
         * customer who had just paid landed on the not-found page. seller-ui has the same gap: its
         * routes are under `src/app/public/`, a *source folder*, mounted at `''`. The prefix in the
         * server constant was reading a directory name as a path.
         *
         * Client-rendered: nothing here is worth prerendering, and both outcomes are reachable by
         * URL alone by anyone, which is why the page states no fact it did not re-read.
         */
        path: 'public/subscription/success',
        loadComponent: () =>
          import('@features/subscription/subscription-outcome').then((page) => page.SubscriptionOutcome),
        data: {titleKey: 'route.subscriptionSuccess.title', succeeded: true},
      },
      {
        path: 'public/subscription/fail',
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
    /*
     * Users. Store-scoped like every other console route, because `user-account/list` is: it filters
     * uaa on `{org, store}`, so the page is a reading of the open store's team and not of the
     * organization's.
     */
    path: 'users',
    loadComponent: () => import('@layouts/console-shell/console-shell').then((layout) => layout.ConsoleShell),
    canActivate: [canAccessSecuredPages, consoleContext, requiresStore],
    children: [
      {path: '', redirectTo: 'team', pathMatch: 'full'},
      {
        // An unknown tab falls back to `team` in the page rather than being matched here — a fixed
        // `:tab` list would make adding a tab a two-file change. Same call as the catalogue.
        path: ':tab',
        loadComponent: () => import('@features/users/users').then((page) => page.Users),
        data: {titleKey: 'route.users.title', breadcrumbKey: 'shell.breadcrumb.users'},
      },
    ],
  },
  {
    /*
     * The operator's own account.
     *
     * `requiresStore` is deliberately absent, and it is the only console route without it: a
     * personal page is not a reading of a store, and an account that has not created one yet still
     * has a language and a theme to set.
     */
    path: 'profile',
    loadComponent: () => import('@layouts/console-shell/console-shell').then((layout) => layout.ConsoleShell),
    canActivate: [canAccessSecuredPages, consoleContext],
    children: [
      {
        path: '',
        loadComponent: () => import('@features/profile/profile').then((page) => page.Profile),
        data: {titleKey: 'route.profile.title', breadcrumbKey: 'shell.breadcrumb.profile'},
      },
    ],
  },
  {
    /*
     * Accepting an invitation, and the one console page that cannot sit inside the console shell.
     *
     * An invitee is authenticated and is **not yet a member of the organization**, so `consoleContext`
     * and `requiresStore` would both refuse them — which is exactly why `OrgMemberApi.accept` carries
     * no permission token either. The bearer token in the link is the authorization.
     */
    path: 'accept-invitation',
    loadComponent: () => import('@layouts/auth-shell/auth-shell').then((layout) => layout.AuthShell),
    canActivate: [canAccessSecuredPages],
    children: [
      {
        path: '',
        loadComponent: () =>
          import('@features/auth/accept-invitation/accept-invitation').then((page) => page.AcceptInvitation),
        data: {titleKey: 'route.acceptInvitation.title'},
      },
    ],
  },
  {
    path: 'payments',
    loadComponent: () => import('@layouts/console-shell/console-shell').then((layout) => layout.ConsoleShell),
    canActivate: [canAccessSecuredPages, consoleContext, requiresStore],
    children: [
      {
        path: '',
        loadComponent: () => import('@features/payments/payments').then((page) => page.Payments),
        data: {titleKey: 'route.payments.title', breadcrumbKey: 'shell.breadcrumb.payments'},
      },
    ],
  },
  {
    /*
     * Its own branch rather than a store-management section: a subscription is store-scoped but it is
     * not a *setting*, and the banner links here from every page in the console.
     *
     * **Not `/billing`.** The gateway claims `/billing/**` for the billing service and matches it
     * before the console's catch-all, so that path never reaches this application at all — the route
     * would have been silently unreachable rather than merely oddly named. `GatewayRouteLocatorImpl`
     * reserves `tenancy`, `billing`, `pod-registry`, `uaa` and `spg`; a console route must avoid all
     * five.
     */
    path: 'subscription',
    loadComponent: () => import('@layouts/console-shell/console-shell').then((layout) => layout.ConsoleShell),
    canActivate: [canAccessSecuredPages, consoleContext],
    children: [
      // The section is part of the URL, so a rail tab is linkable and survives a reload — the same
      // shape store management uses.
      {path: '', redirectTo: 'plan', pathMatch: 'full'},
      {
        path: ':section',
        loadComponent: () => import('@features/billing/billing').then((page) => page.Billing),
        // Billing reads a store's subscription, so there has to be a store.
        canActivate: [requiresStore],
        data: {titleKey: 'route.billing.title', breadcrumbKey: 'shell.breadcrumb.billing'},
      },
    ],
  },
  {
    /*
     * The taxonomy: categories, product types, brands and product groups. The tab is a URL segment
     * so it is linkable and survives a reload — the shape store management and billing already use.
     */
    path: 'catalogue',
    loadComponent: () => import('@layouts/console-shell/console-shell').then((layout) => layout.ConsoleShell),
    canActivate: [canAccessSecuredPages, consoleContext, requiresStore],
    children: [
      {path: '', redirectTo: 'categories', pathMatch: 'full'},
      {
        // An unknown tab is caught in the page and replaced with `categories`, rather than
        // matched here — a fixed `:tab` list would make adding a tab a two-file change.
        path: ':tab',
        loadComponent: () => import('@features/catalogue/catalogue').then((page) => page.Catalogue),
        data: {titleKey: 'route.catalogue.title', breadcrumbKey: 'shell.breadcrumb.catalogue'},
      },
    ],
  },
  {
    /*
     * Products. Its own branch rather than a fifth catalogue tab: this is a paged, filtered table
     * with a wizard behind it, and the four taxonomy tabs are small records edited in place. One
     * route with two behaviours that different would be a worse page than two routes.
     */
    path: 'products',
    loadComponent: () => import('@layouts/console-shell/console-shell').then((layout) => layout.ConsoleShell),
    canActivate: [canAccessSecuredPages, consoleContext, requiresStore],
    children: [
      {
        path: '',
        loadComponent: () => import('@features/products/products').then((page) => page.Products),
        data: {titleKey: 'route.products.title', breadcrumbKey: 'shell.breadcrumb.products'},
      },
      // Static before the id param, so this is not read as a product called "new".
      {
        path: 'new',
        loadComponent: () =>
          import('@features/product-form/product-form').then((page) => page.ProductForm),
        data: {titleKey: 'route.newProduct.title', breadcrumbKey: 'shell.breadcrumb.newProduct'},
      },
      {
        path: ':id',
        loadComponent: () =>
          import('@features/product-form/product-form').then((page) => page.ProductForm),
        data: {titleKey: 'route.productForm.title', breadcrumbKey: 'shell.breadcrumb.productForm'},
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
