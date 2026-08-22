import {Observable, of} from 'rxjs';

import type {ConsoleStore, ConsoleUser, NavigationSection, StoreDirectory} from '@models/console';
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
  /** Shaped like what uaa actually yields today: a username, no email. */
  user: ConsoleUser = {name: 'org1-admin', initials: 'OR', email: null};

  /**
   * A nav of its own, not the real one.
   *
   * This used to be `CONSOLE_NAVIGATION` itself, which meant a spec asserting on the rail could not
   * fail however wrong that constant became — the fixture and the thing under test were the same
   * object. Two groups and three items is enough to exercise grouping, an item with a route and an
   * item without one, which is how the shell marks a section that is not built yet.
   */
  navigation: readonly NavigationSection[] = [
    {groupKey: 'shell.nav.group.seller', items: [{labelKey: 'shell.nav.item.home', icon: 'home', route: '/dashboard'}]},
    {
      groupKey: 'shell.nav.group.organization',
      items: [
        {labelKey: 'shell.nav.item.storeManagement', icon: 'building', route: '/store-management'},
        {labelKey: 'shell.nav.item.userManagement', icon: 'users'},
      ],
    },
  ];

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
