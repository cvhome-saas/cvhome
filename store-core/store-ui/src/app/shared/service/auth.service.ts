import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {map, Observable, of} from "rxjs";
import {Router} from "@angular/router";

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private authUser: AuthUser | undefined;

  constructor(private httpClient: HttpClient, private router: Router) {
    /*
            this.authUser = {
              email_verified: false,
              family_name: "",
              given_name: "",
              preferred_username: "",
              sub: "temp"
            }
    */
  }

  getAuthUser(): Observable<AuthUser> {
    if (this.authUser) {
      return of(this.authUser)
    } else {
      return this.httpClient.get<any>("/api/v1/auth/me")
        .pipe(map((it: any) => {
          this.authUser = it.principal.claims;
          return it;
        }))
    }
  }

  getRoles() {
    return {
      isAdmin: true,
      canAccessToOrder: true,
      isAdminCatalogue: true,
      isAdminContent: true,
      isAdminOrder: true,
      isAdminRetail: true,
      isAdminStore: true,
      isCustomer: true,
      isSuperadmin: true,
    };
  }

  logout() {
    this.router.navigate(['external-logout-link'])
  }
}

export interface AuthUser {
  sub: string
  email_verified: boolean,
  preferred_username: string
  given_name: string
  family_name: string
}
