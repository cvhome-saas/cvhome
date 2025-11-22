import {inject, PLATFORM_ID} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivateFn,  RouterStateSnapshot} from "@angular/router";
import {AuthService} from "./auth.service";
import {catchError, map, of} from "rxjs";
import {isPlatformBrowser} from "@angular/common";
import {environment} from "../../../../environments/environment";


export const canAccessSecuredPages: CanActivateFn = (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot,
) => {
  const oauthService: AuthService = inject(AuthService);
  const platformId = inject(PLATFORM_ID);

  return oauthService.getAuthUser().pipe(map(it => true), catchError(
    it => {
      if (isPlatformBrowser(platformId)) {
        const redirectTo = encodeURIComponent(state.url);
        window.location.href = `${environment.LOGIN_URL}?redirectTo=${redirectTo}`;
      }
      return of(false);
    }));
};
