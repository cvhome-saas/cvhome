import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot, UrlTree} from "@angular/router";
import {AuthService} from "./auth.service";
import {catchError, map, Observable, of} from "rxjs";

export function authenticationGuard(): CanActivateFn {
    return (route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree => {
        const oauthService: AuthService = inject(AuthService);
        const router: Router = inject(Router);
        return oauthService.getAuthUser().pipe(map(it => true), catchError(it => of(router.parseUrl('/welcome'))));
    };
}

