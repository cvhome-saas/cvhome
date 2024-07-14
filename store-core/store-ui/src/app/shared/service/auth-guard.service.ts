import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot} from "@angular/router";
import {AuthService} from "./auth.service";
import {catchError, map, of} from "rxjs";

const PREV_PAGE_KEY = "PREV_PAGE";

export const canAccessSecuredPages: CanActivateFn = (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot,
) => {
  const oauthService: AuthService = inject(AuthService);
  const router: Router = inject(Router);
  return oauthService.getAuthUser().pipe(map(it => true), catchError(
    it => {
      localStorage.setItem(PREV_PAGE_KEY, state.url);
      return of(router.parseUrl('external-login-link'));
    }));
};
