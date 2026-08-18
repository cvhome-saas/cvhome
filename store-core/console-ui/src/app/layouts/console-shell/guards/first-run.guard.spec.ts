import {Component} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {Router, provideRouter} from '@angular/router';
import {Observable, of} from 'rxjs';

import type {StoreDirectory} from '@models/console';
import {ConsoleApi} from '../services/console.api.service';
import {firstRunOnly, requiresStore} from './first-run.guard';

@Component({selector: 'app-stub', template: ''})
class Stub {}

/** Answers the directory question the guards ask, without the fixture or its latency. */
class FakeConsoleApi {
  stores: {id: string; name: string}[] = [];

  loadStores(): Observable<StoreDirectory> {
    const current = this.stores[0]?.id ?? null;
    return of({stores: this.stores, defaultStoreId: current, currentStoreId: current});
  }
}

describe('first-run guards', () => {
  let api: FakeConsoleApi;
  let router: Router;

  beforeEach(() => {
    api = new FakeConsoleApi();
    TestBed.configureTestingModule({
      providers: [
        {provide: ConsoleApi, useValue: api},
        provideRouter([
          {path: 'getting-started', component: Stub, canActivate: [firstRunOnly]},
          {path: 'dashboard', component: Stub, canActivate: [requiresStore]},
          {path: 'orders', component: Stub, canActivate: [requiresStore]},
          {path: 'store-management/create', component: Stub},
          {path: 'store-management/:section', component: Stub, canActivate: [requiresStore]},
        ]),
      ],
    });
    router = TestBed.inject(Router);
    TestBed.createComponent(Stub).detectChanges();
  });

  /**
   * These cases are `async`, not `fakeAsync`: the guards resolve `ConsoleApi` through a
   * dynamic `import()`, which is a real module load that a fake clock cannot flush.
   * Awaiting the navigation is what actually waits for the guard to answer.
   */
  async function go(url: string): Promise<string> {
    await router.navigateByUrl(url);
    return router.url;
  }

  describe('with no store', () => {
    it('holds every console page on getting-started', async () => {
      expect(await go('/dashboard')).toBe('/getting-started');
      expect(await go('/orders')).toBe('/getting-started');
      expect(await go('/store-management/branding')).toBe('/getting-started');
    });

    it('lets getting-started itself through', async () => {
      expect(await go('/getting-started')).toBe('/getting-started');
    });

    it('leaves store creation reachable — it is the only way out', async () => {
      expect(await go('/store-management/create')).toBe('/store-management/create');
    });
  });

  describe('once a store exists', () => {
    beforeEach(() => {
      api.stores = [{id: '65f023632bc46470c104b76f', name: 'Acme Supply Co.'}];
    });

    it('lets the console through', async () => {
      expect(await go('/dashboard')).toBe('/dashboard');
      expect(await go('/orders')).toBe('/orders');
      expect(await go('/store-management/branding')).toBe('/store-management/branding');
    });

    it('sends getting-started to the dashboard, since it has nothing left to say', async () => {
      expect(await go('/getting-started')).toBe('/dashboard');
    });
  });

  it('follows the directory rather than a decision cached at startup', async () => {
    expect(await go('/dashboard')).toBe('/getting-started');

    api.stores = [{id: '65f023632bc46470c104b76f', name: 'Acme Supply Co.'}];

    expect(await go('/dashboard')).toBe('/dashboard');
  });
});
