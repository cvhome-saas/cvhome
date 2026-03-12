import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {Observable} from 'rxjs';
import {AuthService} from '../services/auth.service';
import {map} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {
  }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    const expectedRoles = route.data.expectedRoles;

    return this.authService.getAuthUser().pipe(
      map(user => {
        const roles = this.authService.getRoles();
        const hasRole = expectedRoles.some(role => roles[role]);
        if (hasRole) {
          return true;
        } else {
          // Redirect to an unauthorized page or home
          return this.router.createUrlTree(['/']);
        }
      })
    );
  }
}
