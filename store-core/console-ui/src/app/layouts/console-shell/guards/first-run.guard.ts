import {EnvironmentInjector, inject} from '@angular/core';
import {CanActivateFn, Router, UrlTree} from '@angular/router';
import {Observable, defer, map, switchMap} from 'rxjs';

/** Where a store-less operator is held until they have provisioned one. */
export const FIRST_RUN_ROUTE = '/getting-started';

/** Where an operator who already has a store belongs instead. */
const CONSOLE_HOME = '/dashboard';

/**
 * How many stores this account owns.
 *
 * `ConsoleApi` is imported dynamically rather than at the top of the file: guards are
 * referenced from the eagerly-loaded route table, so a static import would pull the console
 * chrome and its fixtures into the initial bundle — the one marketing and the sign-in pages
 * are prerendered from, and which has no use for either. Every route these guards protect
 * loads that chunk a moment later anyway.
 */
function storeCount(): Observable<number> {
  const injector = inject(EnvironmentInjector);

  return defer(() => import('../services/console.api.service')).pipe(
    switchMap((module) => injector.get(module.ConsoleApi).loadStores()),
    map((directory) => directory.stores.length),
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
