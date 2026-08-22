import {Route} from '@angular/router';

import {canAccessSecuredPages} from '@core/auth/auth-guard.service';
import {consoleContext, requiresStore} from '@layouts/console-shell/guards/first-run.guard';
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
  const CONSOLE_ROUTES = ['getting-started', 'dashboard', 'orders', 'store-management'];
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
});
