import {Injectable, inject} from '@angular/core';
import {map, Observable, of} from "rxjs";
import {ApiError} from "../errors/api-error";
import {CLIENT_ERROR_CODES} from "../errors/problem-detail.model";
import {Router} from "@angular/router";
import {Roles} from './roles';
import {CrudService} from "../http/crud.service";

/**
 * Raw shape of what `/api/v1/auth/me` answers — of which there are **two**, because two different
 * controllers serve that path and they return different things.
 *
 * **Through store-core-gateway** (console-ui): `AuthController#me` returns an
 * `OAuth2AuthenticationToken`, so the OIDC principal arrives nested under `principal` with its ID
 * token `claims`.
 *
 * **On uaa itself** (uaa's own console): `AuthController#me` returns
 * `getAuthentication().getPrincipal()` — the principal *unwrapped* — and for a `formLogin` session
 * that is a `UserDetails`: `{username, authorities, enabled, …}` with no `principal` and no `claims`.
 *
 * Both are handled below. Only handling the first is what made uaa's console bounce straight back to
 * the login page after a successful sign-in: the POST authenticated, the guard then read a shape it
 * did not recognise and treated it as "not signed in".
 *
 * Verified against the running stack, because the previous typing was wrong about it. `principal.claims`
 * holds the **ID token's** claims and nothing else — `sub, aud, azp, auth_time, iss, exp, iat, nonce,
 * jti, sid`. It does **not** carry `given_name`, `family_name`, `email`, `preferred_username` or
 * `user_type`, all of which the old `AuthUser` declared and read from there; every one of them was
 * silently `undefined`. The OIDC standard fields are serialized as camelCase getters on the principal
 * itself, and are all null today because uaa does not enrich the ID token — see lessons.md, "Shell —
 * uaa's ID token carries no profile claims".
 */
interface AuthenticationResponse {
  principal?: {
    claims: IdTokenClaims;
    /** OIDC standard fields, serialized as camelCase getters off the merged ID-token claims. */
    givenName: string | null;
    familyName: string | null;
    email: string | null;
    /** The human handle. The account id is `name`, so this is what a person should be shown. */
    preferredUsername: string | null;
    /** The OIDC subject — the account id, not a name. */
    name: string;
  };
  /** Present on the *session* shape below, where the principal is not wrapped. */
  username?: string;
  /*
   * uaa's own `MeResponse`. It has carried the name, the address and the derived roles and
   * permissions since the console gained a settings screen; this client read none of them and
   * reported `givenName: null` for a person whose first name the endpoint had just sent.
   */
  email?: string | null;
  firstName?: string | null;
  lastName?: string | null;
  roles?: string[];
  permissions?: string[];
  authorities: {authority: string}[];
}

/** What the ID token actually carries. */
interface IdTokenClaims {
  sub: string;
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
          // Both endpoints answer 200 with an *empty body* when nobody is signed in, not a 401.
          // Reading into it then threw a TypeError, and the guard's catch-all treated that as
          // "redirect to login" — the login flow worked by accident. Naming the condition makes it a
          // typed failure the guard can branch on, and keeps a genuine 500 from being read as a logout.
          const principal = it?.principal;
          if (principal?.claims) {
            // The gateway's OAuth2AuthenticationToken.
            this.authUser = {
              sub: principal.claims.sub,
              // `name` is the OIDC subject, which is the account id — a UUID, and not a thing to show
              // anybody. `preferredUsername` is the human handle, and uaa writes it into every ID token.
              username: principal.preferredUsername ?? principal.name,
              givenName: principal.givenName,
              familyName: principal.familyName,
              email: principal.email,
              roles: it.roles ?? [],
              permissions: it.permissions ?? [],
              authorities: it.authorities.map(a => a.authority),
            };
          } else if (it?.username) {
            // uaa's own form-login session. There is no ID token, so there are no profile claims and
            // no `sub`: the username is the whole identity, which is what the JWT carries anyway —
            // see lessons.md, "Users — the JWT carries no user id".
            this.authUser = {
              sub: it.username,
              username: it.username,
              givenName: it.firstName ?? null,
              familyName: it.lastName ?? null,
              email: it.email ?? null,
              roles: it.roles ?? [],
              permissions: it.permissions ?? [],
              authorities: (it.authorities ?? []).map(a => a.authority),
            };
          } else {
            throw new ApiError({
              code: CLIENT_ERROR_CODES.SESSION_MISSING,
              category: 'UNAUTHENTICATED',
              status: 401,
              url: "/api/v1/auth/me",
            });
          }
          return this.authUser;
        }))
    }
  }

  /**
   * The cached principal, without a request, or `undefined` before one has been fetched.
   *
   * `canAccessSecuredPages` fetches and caches it before any console route renders, so on a guarded
   * page this always answers — and it answers `undefined` anywhere else, which is the honest result
   * rather than a promise. `getRoles()` below has read the same cache since Module 1; this exposes
   * the rest of it, because the username is the **only** identity a row and a token share: the JWT
   * `sub` is the username, not uaa's id. See lessons.md, "Users — the JWT carries no user id".
   */
  getCachedAuthUser(): AuthUser | undefined {
    return this.authUser;
  }

  getRoles(): Roles {
    const authorities = this.authUser?.authorities ?? [];
    const isSuperAdmin = authorities.indexOf("ROLE_SUPER_ADMIN") != -1;
    const isSupport = authorities.indexOf("ROLE_SUPPORT") != -1;
    const isOrgAdmin = authorities.indexOf("ROLE_ORG_ADMIN") != -1;
    const isStoreAdmin = authorities.indexOf("ROLE_STORE_ADMIN") != -1;
    const isStoreModerator = authorities.indexOf("ROLE_STORE_MODERATOR") != -1;

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

/**
 * The signed-in principal, as far as uaa will describe it.
 *
 * The nullability is not defensive typing — it is what the endpoint returns. Behind the gateway the
 * profile fields come from the ID token and are still null; on uaa's own session they are populated,
 * because `MeResponse` sends them.
 */
export interface AuthUser {
  readonly sub: string;
  /** uaa's username, e.g. `org1-admin`. The only identity a row and a token share. */
  readonly username: string;
  readonly givenName: string | null;
  readonly familyName: string | null;
  readonly email: string | null;
  /** Bare role names, e.g. `SUPER_ADMIN`. Empty where the endpoint does not send them. */
  readonly roles?: readonly string[];
  /** Effective permission keys, e.g. `users:read`. Empty where the endpoint does not send them. */
  readonly permissions?: readonly string[];
  readonly authorities: readonly string[];
}
