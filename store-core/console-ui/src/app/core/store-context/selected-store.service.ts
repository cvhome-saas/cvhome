import {Injectable, inject} from '@angular/core';

import {BrowserStorage} from '@core/platform/browser-storage';
import {FirstRunMock} from '@core/store-context/first-run-mock';

export interface SelectedStore {
  readonly id: string;
  readonly name: string;
  readonly podId: {
    readonly id: string;
  };
}

const STORES: readonly SelectedStore[] = [
  {id: '65f023632bc46470c104b76f', name: 'Acme Supply Co.', podId: {id: '507f1f77bcf86cd799439011'}},
  {id: '65f023632bc46470c104b75f', name: 'Acme Outlet - West', podId: {id: '507f1f77bcf86cd799439011'}},
  {id: '65f023632bc46470c104b77f', name: 'Acme Wholesale', podId: {id: '507f1f77bcf86cd799439011'}},
];

/** The pod a store provisioned in this session lands on. One pod is all the mocks model. */
const DEFAULT_POD_ID = '507f1f77bcf86cd799439011';

@Injectable({providedIn: 'root'})
export class SelectedStoreService {
  private readonly storage = inject(BrowserStorage);
  private readonly firstRunMock = inject(FirstRunMock);

  /**
   * Mutable because a store created during first run has to become addressable — the
   * request context reads this list to stamp `?store=&pod=`, so a store missing from it
   * cannot be queried. Empty under the first-run mock, which is what makes the zero-store
   * state reachable at all.
   */
  private list: readonly SelectedStore[] = this.firstRunMock.active() ? [] : STORES;

  get stores(): readonly SelectedStore[] {
    return this.list;
  }

  /**
   * The open store, or `null` when the account has none — which is the whole first-run
   * condition, so callers must handle it rather than assuming a first element.
   */
  currentSelectedStore(): SelectedStore | null {
    const id = this.storage.getItem('cvhome.console.store');
    const fallback = this.list.length ? this.list[0].id : null;
    const resolved = id ?? fallback;
    return resolved ? this.getStore(resolved) : null;
  }

  getStore(id: string): SelectedStore | null {
    return this.list.find((store) => store.id === id) ?? null;
  }

  selectStore(id: string): void {
    if (this.getStore(id)) {
      this.storage.setItem('cvhome.console.store', id);
    }
  }

  /** Registers a newly provisioned store and opens it. */
  addStore(id: string, name: string): SelectedStore {
    const store: SelectedStore = {id, name, podId: {id: DEFAULT_POD_ID}};
    this.list = [...this.list, store];
    this.selectStore(id);
    return store;
  }
}
