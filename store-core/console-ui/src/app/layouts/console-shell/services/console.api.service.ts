import {Injectable, inject} from '@angular/core';
import {Observable, map, of} from 'rxjs';

import {ManagerStoreService} from '@api/tenancy/manager-store.service';
import {UserService} from '@core/auth/user.service';
import {SelectedStoreService} from '@api/tenancy/selected-store.service';
import {CONSOLE_NAVIGATION} from '@mocks/console.fixture';
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
  private readonly users = inject(UserService);

  readonly navigation = CONSOLE_NAVIGATION;

  /**
   * The signed-in operator.
   *
   * Initials are derived here rather than sent: they are a rendering of the name, and a server that
   * computed them would have to agree with the console about a rule it has no reason to know.
   */
  loadUser(): Observable<ConsoleUser> {
    return this.users.getCurrentAccount().pipe(
      map((account) => {
        const name = [account.firstName, account.lastName].filter(Boolean).join(' ').trim();
        return {
          name: name || account.emailAddress,
          initials: initialsOf(account.firstName, account.lastName, account.emailAddress),
          email: account.emailAddress,
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

/** First letters of the given and family name, or the email's first two, upper-cased. */
function initialsOf(firstName: string | undefined, lastName: string | undefined, email: string): string {
  const letters = [firstName?.trim()[0], lastName?.trim()[0]].filter(Boolean).join('');
  return (letters || email.slice(0, 2)).toUpperCase();
}
