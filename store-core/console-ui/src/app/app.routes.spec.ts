import {Route} from '@angular/router';

import {canAccessSecuredPages} from '@core/auth/auth-guard.service';
import {
  consoleContext,
  merchantOnly,
  platformOnly,
  requiresStore,
} from '@layouts/console-shell/guards/first-run.guard';
import {routes} from './app.routes';
import {serverRoutes} from './app.routes.server';

/**
 * The console's routes are data, and the one thing that must be true of all of them is that none is
 * reachable signed out. That was not true before this module: `canAccessSecuredPages` existed and
 * `app.routes.ts` referenced it nowhere, so every console page rendered fixtures to anyone.
 *
 * Asserted over the table rather than by navigating, because the failure being guarded against is a new
 * route being added without the guard — which no per-route spec would catch.
 */
describe('app routes', () => {
  const CONSOLE_ROUTES = ['getting-started', 'dashboard', 'orders', 'store-management', 'platform'];
  const PUBLIC_ROUTES = ['', 'sign-in', 'sign-up', 'external-logout-link'];

  function find(path: string): Route {
    const route = routes.find((candidate) => candidate.path === path);
    expect(route).withContext(`no route for "${path}"`).toBeDefined();
    return route!;
  }

  for (const path of CONSOLE_ROUTES) {
    it(`requires authentication for /${path}`, () => {
      expect(find(path).canActivate).toContain(canAccessSecuredPages);
    });
  }

  for (const path of PUBLIC_ROUTES) {
    it(`leaves /${path || '(home)'} open`, () => {
      expect(find(path).canActivate ?? []).not.toContain(canAccessSecuredPages);
    });
  }

  it('gates the console on authentication before it gates on a store', () => {
    // The other order sends a signed-out visitor to getting-started, which then bounces them to uaa —
    // one wasted render, and a page they were never entitled to see.
    const guards = find('dashboard').canActivate ?? [];
    expect(guards.indexOf(canAccessSecuredPages)).toBeLessThan(guards.indexOf(requiresStore));
  });

  for (const path of CONSOLE_ROUTES) {
    it(`loads the store list before /${path} activates`, () => {
      // The request context reads the list synchronously on every later request, and the rail renders
      // on every console page — including the one that does not require a store.
      expect(find(path).canActivate).toContain(consoleContext);
    });
  }

  it('keeps store creation reachable without a store — it is the only way out of first run', () => {
    const branch = find('store-management');
    const create = branch.children?.find((child) => child.path === 'create');

    expect(create).toBeDefined();
    expect(create!.canActivate ?? []).not.toContain(requiresStore);
    // Still behind authentication, and still loading the rail's stores, via the parent.
    expect(branch.canActivate).toContain(canAccessSecuredPages);
    expect(branch.canActivate).toContain(consoleContext);
  });

  /*
   * Every branch says how it renders, in the file that decides it.
   *
   * `subscription`, `public/subscription` and `external-logout-link` were declared nowhere and
   * rendered correctly anyway, because the catch-all at the foot of `app.routes.server.ts` is
   * `Client`. Right answer, arrived at by accident — and silent, so the next branch added would
   * inherit it too. This turns that class of omission into a failing spec.
   */
  describe('render modes', () => {
    const declared = new Set(serverRoutes.map((route) => route.path.replace(/\/\*\*$/, '')));

    const topLevel = routes
      .map((route) => route.path ?? '')
      .filter((path) => path !== '**');

    for (const path of topLevel) {
      it(`declares a render mode for /${path || '(home)'}`, () => {
        expect(declared.has(path)).withContext(`add "${path}" to serverRoutes`).toBeTrue();
      });
    }

    it('keeps the catch-all last, so a declared branch is never shadowed by it', () => {
      expect(serverRoutes.at(-1)?.path).toBe('**');
      expect(serverRoutes.filter((route) => route.path === '**').length).toBe(1);
    });
  });

  /*
   * The platform half of the console. Two properties, and the second is the one worth a spec: these
   * pages are **not** readings of one shop, so `requiresStore` must stay off them — a super admin's
   * store list is not their own anyway. See lessons.md, "Shell — a super admin's store rail is the
   * whole platform, truncated".
   */
  describe('the platform branch', () => {
    it('is gated on the platform role', () => {
      expect(find('platform').canActivate).toContain(platformOnly);
    });

    it('carries no store requirement, on the branch or on any child', () => {
      const branch = find('platform');
      expect(branch.canActivate ?? []).not.toContain(requiresStore);
      for (const child of branch.children ?? []) {
        expect(child.canActivate ?? [])
          .withContext(`platform/${child.path} must not require a store`)
          .not.toContain(requiresStore);
      }
    });

    it('routes every screen the Platform nav group offers', () => {
      const paths = (find('platform').children ?? []).map((child) => child.path);
      expect(paths).toContain('');
      expect(paths).toContain('organizations');
      expect(paths).toContain('pods');
      expect(paths).toContain('users');
    });

    // Static before the id param, or a pod called "new" would swallow the create route.
    it('declares pods/new before pods/:id', () => {
      const paths = (find('platform').children ?? []).map((child) => child.path ?? '');
      expect(paths.indexOf('pods/new')).toBeLessThan(paths.indexOf('pods/:id'));
    });
  });

  /*
   * The mirror of the platform branch's guard, and the one that is easy to forget when adding a
   * page: a merchant screen is a reading of one store, and a platform operator's store list is a
   * truncated page of every tenant's rather than their own — so `requiresStore` passes for them and
   * the page then answers 403 on the merchant's own permission check. Asserted over the table
   * rather than by navigating, because the failure being guarded against is a *new* route being
   * added without it.
   */
  describe('the merchant branches', () => {
    /** Every route in the table that requires a store, parent or child, with its path. */
    function storeScoped(): {path: string; guards: readonly unknown[]}[] {
      return routes.flatMap((route) => {
        const own = {path: route.path ?? '', guards: route.canActivate ?? []};
        const children = (route.children ?? []).map((child) => ({
          path: `${route.path ?? ''}/${child.path ?? ''}`,
          guards: [...(route.canActivate ?? []), ...(child.canActivate ?? [])],
        }));
        return [own, ...children].filter((entry) => entry.guards.includes(requiresStore));
      });
    }

    it('finds the store-scoped routes, so the case below is not vacuous', () => {
      expect(storeScoped().length).toBeGreaterThan(5);
    });

    for (const {path, guards} of storeScoped()) {
      it(`keeps a platform operator out of /${path}`, () => {
        expect(guards)
          .withContext(`/${path} requires a store, so it must also carry merchantOnly`)
          .toContain(merchantOnly);
      });
    }
  });
});
