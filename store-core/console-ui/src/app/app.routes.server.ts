import { RenderMode, ServerRoute } from '@angular/ssr';

/**
 * How each branch is rendered.
 *
 * **One entry per top-level branch, in one style.** This file used to mix three: `orders` and
 * `products` declared both their bare path and `/**`, `catalogue` and `store-management` declared
 * only `/**`, and `subscription`, `public/subscription` and `external-logout-link` were not declared
 * at all — they worked because the catch-all at the bottom is `Client`, which is the right answer
 * arrived at by accident. `app.routes.spec.ts` now fails if a top-level path in `routes` has no
 * entry here, so the accident cannot recur.
 *
 * Everything under the console is `Client` for one reason: `SelectedStoreRequestContext.params()`
 * reads the store list synchronously and throws during SSR.
 */
export const serverRoutes: ServerRoute[] = [
  {
    path: '',
    renderMode: RenderMode.Prerender,
  },
  {
    path: 'sign-in',
    renderMode: RenderMode.Prerender,
  },
  {
    path: 'sign-up',
    renderMode: RenderMode.Prerender,
  },
  {
    path: 'terms',
    renderMode: RenderMode.Prerender,
  },
  {
    path: 'privacy-policy',
    renderMode: RenderMode.Prerender,
  },
  {
    // Client-only like the rest of the console: its guard asks the store directory, which
    // is per-account state the server does not hold.
    path: 'getting-started/**',
    renderMode: RenderMode.Client,
  },
  {
    path: 'dashboard/**',
    renderMode: RenderMode.Client,
  },
  {
    // The platform console. Client-only like the rest: `SelectedStoreRequestContext.params()` reads
    // the store list synchronously and throws during SSR, and the guard reads token claims the
    // server does not hold.
    path: 'platform/**',
    renderMode: RenderMode.Client,
  },
  {
    path: 'orders/**',
    renderMode: RenderMode.Client,
  },
  {
    path: 'payments/**',
    renderMode: RenderMode.Client,
  },
  {
    path: 'customers/**',
    renderMode: RenderMode.Client,
  },
  {
    path: 'shoppers/**',
    renderMode: RenderMode.Client,
  },
  {
    path: 'users/**',
    renderMode: RenderMode.Client,
  },
  {
    path: 'profile/**',
    renderMode: RenderMode.Client,
  },
  {
    path: 'accept-invitation',
    renderMode: RenderMode.Client,
  },
  {
    // The builder is its own shell-free top-level route; same store-scoped SSR constraint.
    path: 'store-management/builder',
    renderMode: RenderMode.Client,
  },
  {
    // Required, not optional: `SelectedStoreRequestContext.params()` throws during SSR.
    path: 'store-management/**',
    renderMode: RenderMode.Client,
  },
  {
    // Same reason: every catalog call is scoped by the request context's `store`.
    path: 'catalogue/**',
    renderMode: RenderMode.Client,
  },
  {
    path: 'products/**',
    renderMode: RenderMode.Client,
  },
  {
    // Content management: every call is scoped by the request context's `store`.
    path: 'content/**',
    renderMode: RenderMode.Client,
  },
  {
    // The billing page. Store-scoped like the rest, and previously declared nowhere — it worked
    // only because it fell through to the catch-all below, which meant the file said nothing about
    // the one console branch a paying customer lands on.
    path: 'subscription/**',
    renderMode: RenderMode.Client,
  },
  {
    // Where Stripe returns a customer who has just paid. `app.routes.ts` calls it client-rendered
    // in a comment; this is the file that decides it.
    path: 'public/subscription/**',
    renderMode: RenderMode.Client,
  },
  {
    // Hands off to uaa's end-session endpoint, so there is nothing to prerender.
    path: 'external-logout-link',
    renderMode: RenderMode.Client,
  },
  {
    path: '**',
    renderMode: RenderMode.Client,
  },
];
