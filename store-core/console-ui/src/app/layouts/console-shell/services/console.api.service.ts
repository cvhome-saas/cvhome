import {Injectable, inject} from '@angular/core';
import {Observable, map, of} from 'rxjs';

import {ManagerStoreService} from '@api/tenancy/manager-store.service';
import {AuthService} from '@cvhome-saas/ui-kit';
import {ConsolePermissions} from '@shared/auth/console-permissions';
import {SelectedStoreService} from '@api/tenancy/selected-store.service';
import {CONSOLE_NAVIGATION} from '../console-navigation';
import type {ConsoleStore, ConsoleUser, StoreDirectory} from '@models/console';
import type {CreateStoreRequest, ManagerStore} from '@models/tenancy';

/**
 * Supplies the console chrome: identity, navigation and stores.
 *
 * Navigation stays a constant. It is a map of this application — which pages exist and how they are
 * grouped — not data any service owns, and an endpoint for it would only let the backend break the
 * front end's routing.
 */
@Injectable({providedIn: 'root'})
export class ConsoleApi {
  private readonly selection = inject(SelectedStoreService);
  private readonly stores = inject(ManagerStoreService);
  private readonly auth = inject(AuthService);
  private readonly permissions = inject(ConsolePermissions);

  readonly navigation = CONSOLE_NAVIGATION;

  /**
   * Whether this account administers the platform rather than a shop.
   *
   * Asked through here rather than injected into the shell facade directly, and the reason is
   * testability rather than taste: `ConsolePermissions` reaches `AuthService`, which reaches
   * `CrudService`, which needs `HttpClient` — so a facade that injected it would drag the whole HTTP
   * stack into every spec that renders the chrome. `ConsoleApi` is the seam those specs already fake,
   * which is exactly what a seam is for.
   *
   * It costs no request: `canAccessSecuredPages` fetches and caches the principal before any console
   * route renders, and this reads that cache.
   */
  canAdministerPlatform(): boolean {
    return this.permissions.canAdministerPlatform();
  }

  /**
   * The signed-in operator, as far as uaa will describe them.
   *
   * In practice that is the **username** and nothing else. Verified against the running stack: the ID
   * token carries only `sub, aud, azp, auth_time, iss, exp, iat, nonce, jti, sid`, and the principal's
   * `givenName`, `familyName` and `email` are all null. So the toolbar shows `org1-admin`, not a person.
   *
   * `GET /tenancy/api/v1/user-account/current` would have the real name — and is broken for every
   * caller: it binds `@AuthenticationPrincipal Principal`, and a JWT principal is a `Jwt`, which does
   * not implement `java.security.Principal`, so the parameter is null and the method NPEs to a 500.
   * Both gaps are in lessons.md.
   *
   * `auth/me` also costs nothing here: the auth guard fetches and caches it before any console route
   * renders.
   */
  loadUser(): Observable<ConsoleUser> {
    return this.auth.getAuthUser().pipe(
      map((account) => {
        const name = [account.givenName, account.familyName].filter(Boolean).join(' ').trim();
        return {
          name: name || account.username,
          initials: initialsOf(account.givenName, account.familyName, account.username),
          email: account.email,
        };
      }),
    );
  }

  /**
   * The stores this operator may switch to.
   *
   * Read from `SelectedStoreService`'s cache rather than fetched: the guard has already loaded it by the
   * time any console page renders, and a second fetch here could disagree with the list the request
   * context is stamping onto every other request.
   */
  loadStores(): Observable<StoreDirectory> {
    return of<StoreDirectory>({
      stores: this.selection.stores.map(toConsoleStore),
      currentStoreId: this.selection.currentSelectedStore()?.id ?? null,
    });
  }

  /**
   * Creates a store and opens it.
   *
   * Answers as soon as the row exists — provisioning runs on after that, and the create page polls
   * `storeInfo` for it. The store is registered locally straight away so the request context can scope
   * that poll.
   */
  createStore(request: CreateStoreRequest): Observable<ManagerStore> {
    return this.stores.create(request).pipe(
      map((store) => {
        this.selection.addStore(store);
        return store;
      }),
    );
  }
}

function toConsoleStore(store: ManagerStore): ConsoleStore {
  return {id: store.id, name: store.name, provisioningState: store.provisioningState, status: store.status};
}

/** First letters of the given and family name, or the username's first two, upper-cased. */
function initialsOf(givenName: string | null, familyName: string | null, username: string): string {
  const letters = [givenName?.trim()[0], familyName?.trim()[0]].filter(Boolean).join('');
  return (letters || username.slice(0, 2)).toUpperCase();
}
