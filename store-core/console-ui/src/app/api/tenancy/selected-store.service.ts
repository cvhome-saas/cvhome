import {Injectable, Injector, inject} from '@angular/core';
import {Observable, shareReplay, tap} from 'rxjs';

import {ManagerStoreService} from '@api/tenancy/manager-store.service';
import {BrowserStorage} from '@core/platform/browser-storage';
import type {ManagerStore} from '@models/tenancy';

/** Which store the console is working in. Per-browser on purpose — see `select`. */
const SELECTED_STORE_KEY = 'cvhome.console.store';

/**
 * The stores this account owns, and which one is open.
 *
 * This service exists to answer one question synchronously: what `?store=` and `?pod=` should be stamped
 * on the request about to go out. `SelectedStoreRequestContext` calls `currentSelectedStore()` from inside
 * `CrudService.getParams()`, which cannot wait for anything.
 *
 * The list, though, arrives over HTTP. The two are reconciled by loading once, before any store-scoped
 * request can be issued — `requiresStore` in the console shell's guard is that point, because it runs
 * before every console route activates and no console page exists outside one. `load()` is idempotent and
 * cached, so the guard may call it on every navigation.
 */
@Injectable({providedIn: 'root'})
export class SelectedStoreService {
  private readonly storage = inject(BrowserStorage);

  /**
   * `ManagerStoreService` is resolved lazily, inside `load()`, because eager injection closes a cycle:
   * it needs `CrudService`, which needs `REQUEST_CONTEXT`, which is `SelectedStoreRequestContext`, which
   * needs this service. Angular reports that as NG0200 at construction — during prerender, before any
   * of it is used. Deferring the lookup to the one method that makes a request breaks the loop without
   * weakening anything: by the time `load()` runs, the whole graph is constructible.
   */
  private readonly injector = inject(Injector);

  /** The cached fetch. Non-null once `load()` has been called, whether or not it has answered yet. */
  private request: Observable<readonly ManagerStore[]> | null = null;

  /**
   * The answer, readable synchronously. Empty before the first load resolves, which is indistinguishable
   * from "this account owns no stores" — which is why nothing may issue a store-scoped request before the
   * guard has resolved `load()`.
   */
  private list: readonly ManagerStore[] = [];

  /**
   * Fetches the store list once per session and remembers it.
   *
   * `shareReplay` rather than a plain `tap`: several guards can resolve concurrently on one navigation,
   * and without it each would issue its own request.
   */
  load(): Observable<readonly ManagerStore[]> {
    this.request ??= this.injector.get(ManagerStoreService).list().pipe(
      tap((stores) => (this.list = stores)),
      shareReplay({bufferSize: 1, refCount: false}),
    );
    return this.request;
  }

  /** Drops the cache so the next `load()` asks again. Called after a store is created. */
  invalidate(): void {
    this.request = null;
  }

  get stores(): readonly ManagerStore[] {
    return this.list;
  }

  /**
   * The open store, or null when the account owns none — which is the whole first-run condition, so
   * callers must handle it rather than assuming a first element.
   *
   * A stored id that is no longer in the list (the store was deleted, or the user signed in as someone
   * else in the same browser) falls back to the first store rather than resolving to nothing: the
   * alternative is a console that looks store-less until the user picks one by hand.
   */
  currentSelectedStore(): ManagerStore | null {
    const stored = this.storage.getItem(SELECTED_STORE_KEY);
    const remembered = stored ? this.getStore(stored) : null;
    return remembered ?? this.list[0] ?? null;
  }

  getStore(id: string): ManagerStore | null {
    return this.list.find((store) => store.id === id) ?? null;
  }

  /**
   * Opens a store.
   *
   * Kept in browser storage rather than sent anywhere: which store an operator is *currently looking at*
   * is a property of this tab, not of the account. A default store that follows them between machines is
   * a different feature, and one the backend cannot support yet — see lessons.md, "Shell — no
   * user-preferences endpoint".
   */
  selectStore(id: string): void {
    if (this.getStore(id)) {
      this.storage.setItem(SELECTED_STORE_KEY, id);
    }
  }

  /**
   * Registers a store that was just created and opens it.
   *
   * Added to the cached list rather than triggering a refetch because the request context reads this list
   * to stamp `?store=&pod=`, and the create page's very next act is to poll the new store — which cannot
   * wait for a second round trip to tenancy.
   */
  addStore(store: ManagerStore): void {
    this.list = [...this.list, store];
    this.selectStore(store.id);
  }
}
