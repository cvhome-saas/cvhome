import {Injectable} from '@angular/core';
import {CanMatch, Route, Router, UrlSegment} from '@angular/router';
import {Observable} from 'rxjs';
import {map, take} from 'rxjs/operators';
import {AuthService} from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class ManagedUserGuard implements CanMatch {
  constructor(private authService: AuthService, private router: Router) {
  }

  canMatch(
    route: Route,
    segments: UrlSegment[]
  ): Observable<boolean> | Promise<boolean> | boolean {
    return this.authService.getAuthUser().pipe(
      take(1),
      map(() => {
        const roles = this.authService.getRoles();
        return !roles.isSuperAdmin;
      })
    );
  }
}
