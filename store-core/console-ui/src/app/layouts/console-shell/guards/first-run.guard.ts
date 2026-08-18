import {inject} from '@angular/core';
import {CanActivateFn, Router, UrlTree} from '@angular/router';
import {Observable, map} from 'rxjs';

import {SelectedStoreService} from '@api/tenancy/selected-store.service';

/** Where a store-less operator is held until they have provisioned one. */
export const FIRST_RUN_ROUTE = '/getting-started';

/** Where an operator who already has a store belongs instead. */
const CONSOLE_HOME = '/dashboard';

/**
 * How many stores this account owns — and, as a side effect, the point at which the store list is
 * fetched at all.
 *
 * This is load-bearing beyond the count. `SelectedStoreRequestContext` reads the list *synchronously*
 * from inside `CrudService.getParams()` to stamp `?store=&pod=`, so the list has to be resolved before
 * any store-scoped request goes out. These guards run before every console route activates and no
 * console page exists outside one, which makes this the one place that is both early enough and never
 * on the path of the prerendered marketing and sign-in routes. `load()` is cached, so calling it on
 * every navigation costs one request per session.
 */
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
