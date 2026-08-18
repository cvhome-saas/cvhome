import {inject} from '@angular/core';
import {CanActivateFn, Router, UrlTree} from '@angular/router';
import {Observable, map} from 'rxjs';

import {SelectedStoreService} from '@api/tenancy/selected-store.service';

/** Where a store-less operator is held until they have provisioned one. */
export const FIRST_RUN_ROUTE = '/getting-started';

/** Where an operator who already has a store belongs instead. */
const CONSOLE_HOME = '/dashboard';

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
  return storeCount().pipe(map((count): boolean | UrlTree => count > 0 || router.createUrlTree([FIRST_RUN_ROUTE])));
};

/**
 * The mirror image: once a store exists, getting-started has nothing left to offer, so it
 * hands over to the console proper rather than lingering as a page the operator can return
 * to and be puzzled by.
 */
export const firstRunOnly: CanActivateFn = () => {
  const router = inject(Router);
  return storeCount().pipe(map((count): boolean | UrlTree => count === 0 || router.createUrlTree([CONSOLE_HOME])));
};
