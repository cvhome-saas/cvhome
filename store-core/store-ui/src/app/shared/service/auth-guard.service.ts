import {inject} from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivateChildFn,
  CanActivateFn,
  Router,
  RouterStateSnapshot,
  UrlTree
} from "@angular/router";
import {AuthService} from "./auth.service";
import {catchError, map, Observable, of} from "rxjs";

export function isLoggedInUser(): CanActivateFn {
  return (route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree => {
    const oauthService: AuthService = inject(AuthService);
    const router: Router = inject(Router);
    return oauthService.getAuthUser().pipe(map(it => true), catchError(
      it => {
        return of(router.parseUrl('external-login-link'));
      })
    );
  };
}

export function isLoggedInUserChild(): CanActivateChildFn {
  return (route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree => {
    return this.isLoggedInUserChild()
  };
}


export const canActivateTeam: CanActivateFn = (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot,
) => {
  const oauthService: AuthService = inject(AuthService);
  const router: Router = inject(Router);
  return oauthService.getAuthUser().pipe(map(it => true), catchError(
    it => {
      return of(router.parseUrl('external-login-link'));
    }));
};
export const canActivateTeamu: CanActivateChildFn = (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot,
) => {
  const oauthService: AuthService = inject(AuthService);
  const router: Router = inject(Router);
  return oauthService.getAuthUser().pipe(map(it => true), catchError(
    it => {
      return of(router.parseUrl('external-login-link'));
    }));
};
