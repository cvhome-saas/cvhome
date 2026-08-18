import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot} from "@angular/router";
import {AuthService} from "./auth.service";
import {catchError, map, of} from "rxjs";
import {toApiError} from "../errors/problem-detail.parser";
import {ApiErrorService} from "../errors/api-error.service";
import {SessionService} from "../errors/session.service";


/**
 * Guards the authenticated area.
 *
 * Previously *any* failure from `getAuthUser()` sent the user to the login URL — a 500, a network blip, a
 * backend still starting up. Against a broken backend that loops forever: log in, fail the check, bounce
 * back to login. Only an authentication failure may redirect now.
 */
export const canAccessSecuredPages: CanActivateFn = (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot,
) => {
  const oauthService: AuthService = inject(AuthService);
  const session = inject(SessionService);
  const apiErrors = inject(ApiErrorService);
  const router = inject(Router);

  return oauthService.getAuthUser().pipe(
    map(() => true),
    catchError((raw: unknown) => {
      // Not annotated as ApiError: an error thrown inside the `map` above never passes through the
      // interceptor, so the annotation would be a cast rather than a guarantee.
      const error = toApiError(raw);
      if (error.isAuth) {
        // The one case a login actually fixes. SessionService owns the redirect and its latch, so several
        // guards resolving at once still produce a single navigation.
        session.onUnauthenticated(error, state.url);
        return of(false);
      }

      // Anything else is the backend's problem, not the seller's credentials. Send them somewhere that
      // renders without auth and say what happened: returning `false` would leave a blank screen on a cold
      // start, with nothing on it to explain why.
      apiErrors.notify(error);
      return of(router.createUrlTree(['/']));
    }));
};
