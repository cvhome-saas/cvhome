import {Injectable, inject} from '@angular/core';
import {Observable, defer, delay, of} from 'rxjs';

import {FirstRunMock} from '@core/store-context/first-run-mock';
import {SelectedStoreService} from '@core/store-context/selected-store.service';
import {
  CONSOLE_DEFAULT_STORE_ID,
  CONSOLE_NAVIGATION,
  CONSOLE_NOTIFICATIONS,
  CONSOLE_ORGANIZATION,
  CONSOLE_STORES,
  CONSOLE_USER,
} from '@mocks/console.fixture';
import type {ConsoleStore, StoreDirectory} from '@models/console';

/** Round-trip the store mocks pretend to take, so loading and pending states are exercised. */
const LATENCY_MS = 250;

/**
 * Supplies the console chrome: identity, navigation, stores and notifications.
 *
 * Everything about stores is already asynchronous, and the ordering and pin state it hands
 * back is held here rather than in the facade — that is server state, and swapping these
 * three methods for `HttpClient` calls should not touch anything above.
 */
@Injectable({providedIn: 'root'})
export class ConsoleApi {
  private readonly selection = inject(SelectedStoreService);
  private readonly firstRunMock = inject(FirstRunMock);

  /** Stands in for the rows a stores endpoint would own, order included. */
  private stores: readonly ConsoleStore[] = this.firstRunMock.active() ? [] : CONSOLE_STORES;
  private defaultStoreId: string | null = this.firstRunMock.active() ? null : CONSOLE_DEFAULT_STORE_ID;

  loadShell() {
    return {
      organization: CONSOLE_ORGANIZATION,
      user: CONSOLE_USER,
      navigation: CONSOLE_NAVIGATION,
      notifications: CONSOLE_NOTIFICATIONS,
    };
  }

  /** The stores this user may switch to, in their saved order. */
  loadStores(): Observable<StoreDirectory> {
    // Deferred so a re-subscribe reads the state as it is now, not as it was at call time.
    return defer(() =>
      of<StoreDirectory>({
        stores: this.stores,
        defaultStoreId: this.defaultStoreId,
        // No first element to fall back on before the account owns a store.
        currentStoreId: this.selection.currentSelectedStore()?.id ?? this.stores[0]?.id ?? null,
      }),
    ).pipe(delay(LATENCY_MS));
  }

  /**
   * Registers a store the operator just provisioned, and opens it.
   *
   * This is what ends first run: the directory stops being empty, so the guards let the
   * rest of the console through. The id is minted here because the mock layer is the only
   * thing standing in for the server that would issue one.
   */
  addStore(name: string): Observable<ConsoleStore> {
    return defer(() => {
      const id = this.mintStoreId();
      const store: ConsoleStore = {id, name};
      this.stores = [...this.stores, store];
      this.defaultStoreId ??= id;
      this.selection.addStore(id, name);
      this.firstRunMock.clear();
      return of(store);
    }).pipe(delay(LATENCY_MS));
  }

  /** Shaped like the 24-character hex ids the rest of the fixtures use. */
  private mintStoreId(): string {
    const random = Math.floor(Math.random() * 0xffffff)
      .toString(16)
      .padStart(6, '0');
    return `${Date.now().toString(16).padStart(12, '0')}${random}`.slice(0, 24).padEnd(24, '0');
  }

  /** Makes a store the one the console opens on. */
  pinDefaultStore(storeId: string): Observable<void> {
    return defer(() => {
      this.defaultStoreId = storeId;
      return of(void 0);
    }).pipe(delay(LATENCY_MS));
  }

  /** Saves the rail's order. Ids not in the list keep their place at the end. */
  reorderStores(storeIds: readonly string[]): Observable<readonly ConsoleStore[]> {
    return defer(() => {
      const ranks = new Map(storeIds.map((id, index) => [id, index]));
      this.stores = [...this.stores].sort(
        (a, b) => (ranks.get(a.id) ?? Number.MAX_SAFE_INTEGER) - (ranks.get(b.id) ?? Number.MAX_SAFE_INTEGER),
      );
      return of(this.stores);
    }).pipe(delay(LATENCY_MS));
  }
}
