import {TestBed} from '@angular/core/testing';
import {Observable, Subject, of} from 'rxjs';

import {BrowserStorage} from '@core/platform/browser-storage';
import type {ManagerStore} from '@models/tenancy';
import {ManagerStoreService} from './manager-store.service';
import {SelectedStoreService} from './selected-store.service';

function store(id: string, name: string): ManagerStore {
  return {
    id,
    name,
    orgId: {id: 'org-1'},
    podId: {id: `pod-for-${id}`},
    provisioningState: 'SUCCESSFULLY_PROVISIONING',
    status: 'ACTIVE',
    billingStatus: 'ACTIVE',
  };
}

const FIRST = store('store-1', 'Acme Supply Co.');
const SECOND = store('store-2', 'Acme Wholesale');

class FakeManagerStoreService {
  calls = 0;
  pending: Subject<ManagerStore[]> | null = null;
  stores: ManagerStore[] = [FIRST, SECOND];

  list(): Observable<ManagerStore[]> {
    this.calls++;
    return this.pending ?? of(this.stores);
  }
}

describe('SelectedStoreService', () => {
  let api: FakeManagerStoreService;
  let service: SelectedStoreService;

  beforeEach(() => {
    localStorage.removeItem('cvhome.console.store');
    api = new FakeManagerStoreService();
    TestBed.configureTestingModule({
      providers: [BrowserStorage, {provide: ManagerStoreService, useValue: api}],
    });
    service = TestBed.inject(SelectedStoreService);
  });

  it('fetches the list once however many times it is asked', () => {
    service.load().subscribe();
    service.load().subscribe();
    service.load().subscribe();

    expect(api.calls).toBe(1);
  });

  it('answers synchronously once loaded, which is what the request context needs', () => {
    expect(service.stores).toEqual([]);

    service.load().subscribe();

    expect(service.stores).toEqual([FIRST, SECOND]);
    expect(service.currentSelectedStore()).toEqual(FIRST);
  });

  it('reports no store before the load answers, rather than guessing one', () => {
    api.pending = new Subject<ManagerStore[]>();
    service.load().subscribe();

    expect(service.currentSelectedStore()).toBeNull();

    api.pending.next([FIRST]);
    expect(service.currentSelectedStore()).toEqual(FIRST);
  });

  it('remembers the open store across loads', () => {
    service.load().subscribe();
    service.selectStore(SECOND.id);

    expect(service.currentSelectedStore()).toEqual(SECOND);
    expect(localStorage.getItem('cvhome.console.store')).toBe(SECOND.id);
  });

  it('refuses to open a store the account does not have', () => {
    service.load().subscribe();
    service.selectStore('store-belonging-to-someone-else');

    expect(service.currentSelectedStore()).toEqual(FIRST);
  });

  it('falls back to the first store when the remembered one is gone', () => {
    // A store deleted, or a different account signed in to the same browser.
    localStorage.setItem('cvhome.console.store', 'store-that-no-longer-exists');
    service.load().subscribe();

    expect(service.currentSelectedStore()).toEqual(FIRST);
  });

  it('registers a created store without waiting for a refetch', () => {
    service.load().subscribe();
    const created = store('store-3', 'Acme Outlet');

    service.addStore(created);

    // The request context has to be able to scope the very next call — the provisioning poll.
    expect(service.getStore(created.id)).toEqual(created);
    expect(service.currentSelectedStore()).toEqual(created);
    expect(api.calls).toBe(1);
  });

  it('asks again after invalidate', () => {
    service.load().subscribe();
    service.invalidate();
    service.load().subscribe();

    expect(api.calls).toBe(2);
  });
});
