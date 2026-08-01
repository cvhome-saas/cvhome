import {Injectable, inject} from '@angular/core';
import {map, Observable, of} from "rxjs";
import {Router} from "@angular/router";
import {Roles} from "../models/roles";
import {CrudService} from "./crud.service";

/** Raw shape of Spring Security's Authentication object as returned by
 *  AuthController#me (declared `Object` on the Java side — genuinely
 *  untyped; this is the default serialization of the JWT-backed principal). */
interface AuthenticationResponse {
  principal: {claims: AuthUser};
  authorities: {authority: string}[];
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly crudService = inject(CrudService);
  private readonly router = inject(Router);

  private authUser: AuthUser | undefined;

  getAuthUser(): Observable<AuthUser> {
    if (this.authUser) {
      return of(this.authUser)
    } else {
      return this.crudService.get<AuthenticationResponse>("/api/v1/auth/me")
        .pipe(map((it) => {
          this.authUser = it.principal.claims;
          this.authUser.authorities = it.authorities.map(a => a.authority)
          return this.authUser;
        }))
    }
  }

  getRoles(): Roles {
    const isSuperAdmin = this.authUser.authorities.indexOf("ROLE_SUPER_ADMIN") != -1;
    const isSupport = this.authUser.authorities.indexOf("ROLE_SUPPORT") != -1;
    const isOrgAdmin = this.authUser.authorities.indexOf("ROLE_ORG_ADMIN") != -1;
    const isStoreAdmin = this.authUser.authorities.indexOf("ROLE_STORE_ADMIN") != -1;
    const isStoreModerator = this.authUser.authorities.indexOf("ROLE_STORE_MODERATOR") != -1;

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
