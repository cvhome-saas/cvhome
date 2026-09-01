import { Routes } from '@angular/router';

import {canAccessSecuredPages} from '@core/auth/auth-guard.service';
import {
  consoleContext,
  firstRunOnly,
  merchantOnly,
  platformOnly,
  requiresStore,
} from '@layouts/console-shell/guards/first-run.guard';

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
    /*
     * The platform console: the other product this application is.
     *
     * Inside the same shell rather than a second one — a platform operator switching between the two
     * halves should not be switching applications — but with a different guard set. `requiresStore`
     * is deliberately **absent**: none of these pages is a reading of one shop, and a super admin's
     * store list is not their own anyway (see lessons.md, "Shell — a super admin's store rail is the
     * whole platform, truncated").
     *
     * `platformOnly` redirects anyone else to the merchant dashboard, rather than rendering a page
     * whose every request will 403.
     */
    path: 'platform',
    loadComponent: () => import('@layouts/console-shell/console-shell').then((layout) => layout.ConsoleShell),
    canActivate: [canAccessSecuredPages, consoleContext, platformOnly],
    children: [
      {
        path: '',
        loadComponent: () =>
          import('@features/platform-dashboard/platform-dashboard').then((page) => page.PlatformDashboard),
        data: {titleKey: 'route.platform.title', breadcrumbKey: 'shell.breadcrumb.platform'},
      },
      {
        path: 'organizations',
        loadComponent: () => import('@features/organizations/organizations').then((page) => page.Organizations),
        data: {titleKey: 'route.organizations.title', breadcrumbKey: 'shell.breadcrumb.organizations'},
      },
      {
        // The tab is part of the URL, so a tab is linkable and survives a reload — the shape store
        // management and billing already use. An unknown tab settles in the page, not here.
        path: 'organizations/:id',
        redirectTo: 'organizations/:id/overview',
        pathMatch: 'full',
      },
      {
        path: 'organizations/:id/:section',
        loadComponent: () =>
          import('@features/organization-detail/organization-detail').then((page) => page.OrganizationDetail),
        data: {titleKey: 'route.organization.title', breadcrumbKey: 'shell.breadcrumb.organization'},
      },
      {
        path: 'pods',
        loadComponent: () => import('@features/pods/pods').then((page) => page.Pods),
        data: {titleKey: 'route.pods.title', breadcrumbKey: 'shell.breadcrumb.pods'},
      },
      // Static before the id param, so this is not read as a pod called "new".
      {
        path: 'pods/new',
        loadComponent: () => import('@features/pod-detail/pod-detail').then((page) => page.PodDetail),
        data: {titleKey: 'route.newPod.title', breadcrumbKey: 'shell.breadcrumb.newPod'},
      },
      {
        path: 'pods/:id',
        loadComponent: () => import('@features/pod-detail/pod-detail').then((page) => page.PodDetail),
        data: {titleKey: 'route.pod.title', breadcrumbKey: 'shell.breadcrumb.pod'},
      },
      {
        path: 'users',
        loadComponent: () => import('@features/platform-users/platform-users').then((page) => page.PlatformUsers),
        data: {titleKey: 'route.platformUsers.title', breadcrumbKey: 'shell.breadcrumb.platformUsers'},
      },
      {
        // The tab is part of the URL, so a filtered register is linkable and survives a reload —
        // the shape the organization detail and store management already use.
        path: 'billing',
        redirectTo: 'billing/overview',
        pathMatch: 'full',
      },
      {
        path: 'billing/:section',
        loadComponent: () =>
          import('@features/platform-billing/platform-billing').then((page) => page.PlatformBilling),
        data: {titleKey: 'route.platformBilling.title', breadcrumbKey: 'shell.breadcrumb.platformBilling'},
      },
      {
        // The plan catalogue. Read-only: plans are created in Stripe and mirrored into `billing.plan`,
        // and inventing a write here would be inventing a second source of truth for what a customer
        // is charged. `/platform/billing` is where the money it describes is actually read.
        path: 'plans',
        loadComponent: () => import('@features/platform-plans/platform-plans').then((page) => page.PlatformPlans),
        data: {titleKey: 'route.plans.title', breadcrumbKey: 'shell.breadcrumb.plans'},
      },
    ],
  },
  {
    path: 'dashboard',
    loadComponent: () => import('@layouts/console-shell/console-shell').then((layout) => layout.ConsoleShell),
    canActivate: [canAccessSecuredPages, consoleContext, merchantOnly, requiresStore],
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
    canActivate: [canAccessSecuredPages, consoleContext, merchantOnly, requiresStore],
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
    canActivate: [canAccessSecuredPages, consoleContext, merchantOnly, requiresStore],
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
    /*
     * Customers. Store-scoped like every other console route, because `CustomerApi.list` is: it
     * filters on `storeMerchantId`, so a shopper who has bought from two of the merchant's stores is
     * a separate record in each.
     *
     * `?customer=` opens one and `?q=` carries the search term. Both are query parameters rather
     * than segments because neither is a different view of the page — and `?q=` is how another
     * screen links *in*, there being no endpoint that fetches a customer by id.
     */
    path: 'customers',
    loadComponent: () => import('@layouts/console-shell/console-shell').then((layout) => layout.ConsoleShell),
    canActivate: [canAccessSecuredPages, consoleContext, merchantOnly, requiresStore],
    children: [
      {
        path: '',
        loadComponent: () => import('@features/customers/customers').then((page) => page.Customers),
        data: {titleKey: 'route.customers.title', breadcrumbKey: 'shell.breadcrumb.customers'},
      },
    ],
  },
  {
    path: 'payments',
    loadComponent: () => import('@layouts/console-shell/console-shell').then((layout) => layout.ConsoleShell),
    canActivate: [canAccessSecuredPages, consoleContext, merchantOnly, requiresStore],
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
    // `merchantOnly` on the parent rather than beside the child's `requiresStore`: a subscription
    // belongs to a store, and a platform operator has none of their own to be billed for.
    canActivate: [canAccessSecuredPages, consoleContext, merchantOnly],
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
    canActivate: [canAccessSecuredPages, consoleContext, merchantOnly, requiresStore],
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
    canActivate: [canAccessSecuredPages, consoleContext, merchantOnly, requiresStore],
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
    /*
     * Content management: the hub with its seven tabs at `content/:tab`, and one editor route per
     * workflow type (`content/pages/new`, `content/pages/:id`, …). The editor routes come first so
     * `pages/new` is not read as the tab `pages` with a stray segment — a static path beats a param
     * in Angular's matcher, but `:tab` only matches one segment anyway, so the order is for reading.
     */
    path: 'content',
    loadComponent: () => import('@layouts/console-shell/console-shell').then((layout) => layout.ConsoleShell),
    canActivate: [canAccessSecuredPages, consoleContext, merchantOnly, requiresStore],
    children: [
      {path: '', redirectTo: 'pages', pathMatch: 'full'},
      {
        path: 'pages/new',
        loadComponent: () => import('@features/content/editors/page-editor/page-editor').then((page) => page.PageEditor),
        data: {titleKey: 'route.content.newPage', breadcrumbKey: 'shell.breadcrumb.content'},
      },
      {
        path: 'pages/:id',
        loadComponent: () => import('@features/content/editors/page-editor/page-editor').then((page) => page.PageEditor),
        data: {titleKey: 'route.content.page', breadcrumbKey: 'shell.breadcrumb.content'},
      },
      {
        path: 'posts/new',
        loadComponent: () => import('@features/content/editors/post-editor/post-editor').then((page) => page.PostEditor),
        data: {titleKey: 'route.content.newPost', breadcrumbKey: 'shell.breadcrumb.content'},
      },
      {
        path: 'posts/:id',
        loadComponent: () => import('@features/content/editors/post-editor/post-editor').then((page) => page.PostEditor),
        data: {titleKey: 'route.content.post', breadcrumbKey: 'shell.breadcrumb.content'},
      },
      {
        path: 'banners/new',
        loadComponent: () => import('@features/content/editors/banner-editor/banner-editor').then((page) => page.BannerEditor),
        data: {titleKey: 'route.content.newBanner', breadcrumbKey: 'shell.breadcrumb.content'},
      },
      {
        path: 'banners/:id',
        loadComponent: () => import('@features/content/editors/banner-editor/banner-editor').then((page) => page.BannerEditor),
        data: {titleKey: 'route.content.banner', breadcrumbKey: 'shell.breadcrumb.content'},
      },
      {
        path: 'faq/new',
        loadComponent: () => import('@features/content/editors/faq-editor/faq-editor').then((page) => page.FaqEditor),
        data: {titleKey: 'route.content.newFaq', breadcrumbKey: 'shell.breadcrumb.content'},
      },
      {
        path: 'faq/:id',
        loadComponent: () => import('@features/content/editors/faq-editor/faq-editor').then((page) => page.FaqEditor),
        data: {titleKey: 'route.content.faq', breadcrumbKey: 'shell.breadcrumb.content'},
      },
      {
        path: 'policies/new',
        loadComponent: () => import('@features/content/editors/policy-editor/policy-editor').then((page) => page.PolicyEditor),
        data: {titleKey: 'route.content.newPolicy', breadcrumbKey: 'shell.breadcrumb.content'},
      },
      {
        path: 'policies/:id',
        loadComponent: () => import('@features/content/editors/policy-editor/policy-editor').then((page) => page.PolicyEditor),
        data: {titleKey: 'route.content.policy', breadcrumbKey: 'shell.breadcrumb.content'},
      },
      {
        // An unknown tab is caught in the page and replaced with `pages` — the catalogue's shape.
        path: ':tab',
        loadComponent: () => import('@features/content/content-hub').then((page) => page.ContentHub),
        data: {titleKey: 'route.content.title', breadcrumbKey: 'shell.breadcrumb.content'},
      },
    ],
  },
  {
    // The storefront builder escapes the console shell entirely: an editor wants the whole viewport,
    // and the page brings its own top chrome. Same URL as before, declared ahead of the
    // `store-management` branch so it wins the match.
    path: 'store-management/builder',
    loadComponent: () =>
      import('@features/storefront-builder/storefront-builder').then((page) => page.StorefrontBuilder),
    canActivate: [canAccessSecuredPages, consoleContext, merchantOnly, requiresStore],
    data: {titleKey: 'route.builder.title', breadcrumbKey: 'shell.breadcrumb.builder'},
  },
  {
    path: 'store-management',
    loadComponent: () => import('@layouts/console-shell/console-shell').then((layout) => layout.ConsoleShell),
    // Authentication and the store list for the whole branch; a *store* is not required, because
    // `create` below is the only way out of first run — but the rail still renders on it, so the list
    // has to load there too.
    //
    // `merchantOnly` sits here rather than beside the child's `requiresStore`, and that deliberately
    // takes `create` with it: a platform operator is never in first run, and creating a store as one
    // is broken anyway — `StoreManagerApi.create` scopes to the caller's org and they have none.
    canActivate: [canAccessSecuredPages, consoleContext, merchantOnly],
    children: [
      // Static before the section param, so this does not get swallowed as a section name.
      {
        path: 'create',
        loadComponent: () => import('@features/create-store/create-store').then((page) => page.CreateStore),
        data: {titleKey: 'route.createStore.title', breadcrumbKey: 'shell.breadcrumb.createStore'},
      },
      // The section is part of the URL, so a settings card is linkable and survives a reload.
      // `domain` because it is the first section; `branding` used to be, and stayed here after
      // appearance moved to the content hub — the page then opened on a section that no longer
      // existed and rendered an empty pane.
      {path: '', redirectTo: 'domain', pathMatch: 'full'},
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
