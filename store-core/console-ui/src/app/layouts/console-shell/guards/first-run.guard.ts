import {inject} from '@angular/core';
import {CanActivateFn, Router, UrlTree} from '@angular/router';
import {Observable, catchError, combineLatest, map, of} from 'rxjs';

import {SelectedStoreService} from '@api/tenancy/selected-store.service';
import {AuthService} from '@cvhome-saas/ui-kit';
import {ConsolePermissions} from '@shared/auth/console-permissions';

/** Where a store-less operator is held until they have provisioned one. */
export const FIRST_RUN_ROUTE = '/getting-started';

/** Where an operator who already has a store belongs instead. */
const CONSOLE_HOME = '/dashboard';

/** Where a platform operator belongs: the console's other half. */
export const PLATFORM_HOME = '/platform';

/**
 * Resolves the store list before the route activates, and always allows.
 *
 * This is the load point for the whole console. `SelectedStoreRequestContext` reads the list
 * *synchronously* from inside `CrudService.getParams()` to stamp `?store=&pod=`, so it has to be
 * resolved before any store-scoped request goes out — and the shell's rail has to know the account's
 * stores whether or not the current page needs one.
 *
 * Separate from `requiresStore` because `store-management/create` is reachable *without* a store and
 * still renders the rail: folding the fetch into `requiresStore` left that page telling an account with
 * two stores that it had none.
 *
 * `load()` is cached, so applying this to every console route costs one request per session.
 */
export const consoleContext: CanActivateFn = () =>
  inject(SelectedStoreService)
    .load()
    .pipe(map(() => true));

function storeCount(): Observable<number> {
  return inject(SelectedStoreService)
    .load()
    .pipe(map((stores) => stores.length));
}

/**
 * Whether this account administers the platform, resolved rather than read.
 *
 * **`canActivate` guards run concurrently, not in sequence.** Angular collects a route's guards and
 * subscribes to all of them at once through `prioritizedGuardValue`, which only *reports* them in
 * declaration order — so a guard that reads a value `canAccessSecuredPages` caches sees an empty
 * principal on a cold load and a populated one on every in-app navigation afterwards. That is
 * exactly the shape of bug that passes every click-through and fails on a pasted link: `/platform`
 * typed into the address bar bounced to the dashboard, and the same link clicked in the rail worked.
 *
 * `getAuthUser()` is cached, so awaiting it here costs one request per session and nothing after.
 * A failure answers `false` and lets `canAccessSecuredPages` — which owns that decision — do the
 * redirecting.
 */
function isPlatformOperator(): Observable<boolean> {
  const auth = inject(AuthService);
  const permissions = inject(ConsolePermissions);
  return auth.getAuthUser().pipe(
    map(() => permissions.canAdministerPlatform()),
    catchError(() => of(false)),
  );
}

/**
 * Holds a store-less account on the getting-started page.
 *
 * Every console page is a reading of one store — `SelectedStoreRequestContext` stamps
 * `?store=&pod=` onto each request and throws rather than guess — so a page opened before a
 * store exists has nothing to show and no safe way to ask for it. Redirecting is the honest
 * answer; a screen of empty widgets is not.
 *
 * `store-management/create` deliberately carries no guard: it is the only way out.
 */
export const requiresStore: CanActivateFn = () => {
  const router = inject(Router);
  return combineLatest([storeCount(), isPlatformOperator()]).pipe(
    map(([count, platform]): boolean | UrlTree => {
      if (count > 0) {
        return true;
      }
      /*
       * A platform operator is never held on getting-started. Creating a store is not what they came
       * for, and the page's whole argument — "everything here is a reading of one store" — is the
       * one thing that is not true of them.
       *
       * In practice they are rarely store-*less*: `InternalStoreServiceImpl.findAll` reads a null org
       * claim as platform-wide, so a super admin's list is a truncated page of every tenant's stores.
       * See lessons.md, "Shell — a super admin's store rail is the whole platform, truncated".
       */
      return router.createUrlTree([platform ? PLATFORM_HOME : FIRST_RUN_ROUTE]);
    }),
  );
};

/**
 * The mirror image: once a store exists, getting-started has nothing left to offer, so it
 * hands over to the console proper rather than lingering as a page the operator can return
 * to and be puzzled by.
 */
export const firstRunOnly: CanActivateFn = () => {
  const router = inject(Router);
  return combineLatest([storeCount(), isPlatformOperator()]).pipe(
    map(([count, platform]): boolean | UrlTree => {
      // A platform operator has nothing to do here even with no store of their own, so they go to
      // the platform home rather than the merchant dashboard.
      if (platform) {
        return router.createUrlTree([PLATFORM_HOME]);
      }
      return count === 0 || router.createUrlTree([CONSOLE_HOME]);
    }),
  );
};

/**
 * Holds the platform half of the console to accounts that can actually use it.
 *
 * Every endpoint behind `/platform/*` is super-admin only, so without this a merchant who typed the
 * URL would get a rendered page that 403s row by row — a screen that looks broken rather than one
 * that says no. Redirecting to the dashboard is the honest answer, and it is the same call
 * `requiresStore` makes for a page with no store to read.
 *
 * Reads token claims the client cannot mint, and mirrors the server rather than replacing it.
 */
export const platformOnly: CanActivateFn = () => {
  const router = inject(Router);
  return isPlatformOperator().pipe(
    map((platform): boolean | UrlTree => platform || router.createUrlTree([CONSOLE_HOME])),
  );
};

/**
 * The mirror image: holds the merchant half of the console to accounts that have a shop to read.
 *
 * **Every merchant page is a reading of one store, and a platform operator does not have one.**
 * `InternalStoreServiceImpl.findAll` reads their null org claim as platform-wide and hands them a
 * truncated page of *every tenant's* stores, so the switcher is hidden and the request context
 * resolves `?store=` to whichever stranger's shop sorts first — which then answers 403 on the
 * merchant's own permission check. The dashboard did exactly that before this guard existed: a
 * super admin's home page was an access-denied banner.
 *
 * Sending them to `/platform` is the honest answer. It is the same call `platformOnly` makes in the
 * other direction, and the same one `requiresStore` makes for a merchant with no store yet.
 *
 * **Not on `/profile`**, which is a personal page rather than a reading of a store, and not on
 * `/subscription`'s parent for the same reason its own child carries `requiresStore` instead.
 */
export const merchantOnly: CanActivateFn = () => {
  const router = inject(Router);
  return isPlatformOperator().pipe(
    map((platform): boolean | UrlTree => !platform || router.createUrlTree([PLATFORM_HOME])),
  );
};
