import {Injectable} from '@angular/core';
import {CanActivate, Router, UrlTree} from '@angular/router';
import {Observable} from 'rxjs';
import {AuthService} from '../services/auth.service';
import {map} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class HomeRedirectGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {
  }

  canActivate(): Observable<boolean | UrlTree> {
    return this.authService.getAuthUser().pipe(
      map(() => {
        const roles = this.authService.getRoles();
        if (roles.isSuperAdmin) {
          return this.router.createUrlTree(['/home/sys']);
        } else {
          return this.router.createUrlTree(['/home/managed']);
        }
      })
    );
  }
}
