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
          this.authUser = it.principal.claims;
          this.authUser.authorities = it.authorities.map(a => a.authority)
          return it;
        }))
    }
  }

  getRoles(): Roles {
    let isSuperAdmin = this.authUser.authorities.indexOf("ROLE_SUPER_ADMIN") != -1;
    let isSupport = this.authUser.authorities.indexOf("ROLE_SUPPORT") != -1;
    let isOrgAdmin = this.authUser.authorities.indexOf("ROLE_ORG_ADMIN") != -1;
    let isStoreAdmin = this.authUser.authorities.indexOf("ROLE_STORE_ADMIN") != -1;
    let isStoreModerator = this.authUser.authorities.indexOf("ROLE_STORE_MODERATOR") != -1;

    return {
      isSuperAdmin,
      isSupport,
      isOrgAdmin,
      isStoreAdmin,
      isStoreModerator
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
  user_type: UserType
  authorities: string[]
}

export enum UserType {
  SUPPER_USER = 'SUPPER_USER',
  ORG_USER = 'ORG_USER',
  MANAGED_USER = 'MANAGED_USER'
}
