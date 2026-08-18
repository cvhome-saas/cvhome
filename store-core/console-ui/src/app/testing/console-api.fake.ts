import {Observable, of} from 'rxjs';

import {CONSOLE_NAVIGATION} from '@mocks/console.fixture';
import type {ConsoleStore, ConsoleUser, StoreDirectory} from '@models/console';
import type {ProvisioningState} from '@models/tenancy';

/**
 * Stands in for `ConsoleApi` in shell specs.
 *
 * The real one reaches tenancy for the store list and the signed-in account, so a spec that renders the
 * chrome has to supply this or provide `HttpClient` and a store fixture — and the point of most of these
 * cases is the chrome's behaviour, not its data.
 */
export class FakeConsoleApi {
  stores: readonly ConsoleStore[] = [];
  user: ConsoleUser = {name: 'Jordan Diaz', initials: 'JD', email: 'jordan@acmesupply.co'};

  readonly navigation = CONSOLE_NAVIGATION;

  loadUser(): Observable<ConsoleUser> {
    return of(this.user);
  }

  loadStores(): Observable<StoreDirectory> {
    return of({stores: this.stores, currentStoreId: this.stores[0]?.id ?? null});
  }
}

/** One store as the rail sees it. Provisioned and active unless a case says otherwise. */
export function consoleStore(
  id: string,
  name: string,
  provisioningState: ProvisioningState = 'SUCCESSFULLY_PROVISIONING',
): ConsoleStore {
  return {id, name, provisioningState, status: 'ACTIVE'};
}

/** The three-store account most shell specs assume. */
export const CONSOLE_STORES_FAKE: readonly ConsoleStore[] = [
  consoleStore('65f023632bc46470c104b76f', 'Acme Supply Co.'),
  consoleStore('65f023632bc46470c104b75f', 'Acme Outlet - West'),
  consoleStore('65f023632bc46470c104b77f', 'Acme Wholesale'),
];
