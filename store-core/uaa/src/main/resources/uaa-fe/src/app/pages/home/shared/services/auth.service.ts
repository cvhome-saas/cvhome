import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {map, Observable, of} from "rxjs";
import {Router} from "@angular/router";
import {Roles} from "../models/roles";

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private authUser: AuthUser | undefined;

  constructor(private httpClient: HttpClient, private router: Router) {
    // this.authUser = {
    //   email_verified: false,
    //   family_name: "",
    //   given_name: "",
    //   preferred_username: "",
    //   sub: "temp",
    //   user_type: UserType.ORG_USER,
    //   authorities: [
    //     "ROLE_ORG_ADMIN"
    //   ]
    // }
  }

  getAuthUser(): Observable<AuthUser> {
    if (this.authUser) {
      return of(this.authUser)
    } else {
      return this.httpClient.get<any>("/api/v1/auth/me")
        .pipe(map((it: any) => {
          this.authUser = it;
          this.authUser.authorities = it.authorities.map(a => a.authority)
          return it;
        }))
    }
  }

  getRoles(): Roles {
    let isSuperAdmin = this.authUser.authorities.indexOf("ROLE_SUPER_ADMIN") != -1;

    return {
      isSuperAdmin
    };
  }

  logout() {
    this.router.navigate(['external-logout-link'])
  }
}

export interface AuthUser {
  username: string
  authorities: string[]
}
