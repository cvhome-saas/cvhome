import {Component} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {Router, provideRouter} from '@angular/router';
import {Observable, defer, of} from 'rxjs';

import {SelectedStoreService} from '@api/tenancy/selected-store.service';
import {AuthService} from '@core/auth/auth.service';
import {ConsolePermissions} from '@shared/auth/console-permissions';
import type {ManagerStore} from '@models/tenancy';
import {firstRunOnly, merchantOnly, platformOnly, requiresStore} from './first-run.guard';

@Component({selector: 'app-stub', template: ''})
class Stub {}

/**
 * The principal, fetched once and cached — the half of `AuthService` the guards depend on.
 *
 * The roles become readable only **when this has been subscribed**, which is not a detail of the
 * fake: `AuthService.getRoles()` reads a field `getAuthUser()`'s `map` assigns, so a caller that has
 * not awaited it sees no roles. Reproducing that here is what makes the concurrency case below able
 * to fail.
 */
class FakeAuthService {
  resolved = false;

  getAuthUser(): Observable<unknown> {
    return defer(() => {
      this.resolved = true;
      return of({});
    });
  }
}

/**
 * Answers "may this account administer the platform" without an HTTP stack.
 *
 * Faked rather than provided for real because the real one reaches `AuthService`, which reaches
 * `CrudService`, which needs `HttpClient` — the whole stack, to answer a boolean read from a cached
 * token claim. It answers `false` until the principal has been fetched, exactly as the real one does.
 */
class FakeConsolePermissions {
  platform = false;

  constructor(private readonly auth: FakeAuthService) {}

  canAdministerPlatform(): boolean {
    return this.auth.resolved && this.platform;
  }
}

/**
 * Answers the question the guards ask, without a request.
 *
 * The guards read the store list rather than a directory now, because loading it is the other half of
 * what they are for: the request context reads it synchronously on every later request.
 */
class FakeSelectedStoreService {
  stores: ManagerStore[] = [];
  loads = 0;

  load(): Observable<readonly ManagerStore[]> {
    this.loads++;
    return of(this.stores);
  }
}

function store(id: string, name: string): ManagerStore {
  return {
    id,
    name,
    orgId: {id: 'org-1'},
    podId: {id: 'pod-1'},
    provisioningState: 'SUCCESSFULLY_PROVISIONING',
    status: 'ACTIVE',
    billingStatus: 'ACTIVE',
    provisioningError: null,
  };
}

describe('first-run guards', () => {
  let api: FakeSelectedStoreService;
  let auth: FakeAuthService;
  let permissions: FakeConsolePermissions;
  let router: Router;

  beforeEach(() => {
    api = new FakeSelectedStoreService();
    auth = new FakeAuthService();
    permissions = new FakeConsolePermissions(auth);
    TestBed.configureTestingModule({
      providers: [
        {provide: SelectedStoreService, useValue: api},
        {provide: AuthService, useValue: auth},
        {provide: ConsolePermissions, useValue: permissions},
        provideRouter([
          {path: 'getting-started', component: Stub, canActivate: [firstRunOnly]},
          {path: 'dashboard', component: Stub, canActivate: [requiresStore]},
          {path: 'orders', component: Stub, canActivate: [requiresStore]},
          {path: 'store-management/create', component: Stub},
          {path: 'store-management/:section', component: Stub, canActivate: [requiresStore]},
          {path: 'platform', component: Stub, canActivate: [platformOnly]},
          {path: 'platform/organizations', component: Stub, canActivate: [platformOnly]},
          {path: 'catalogue', component: Stub, canActivate: [merchantOnly, requiresStore]},
        ]),
      ],
    });
    router = TestBed.inject(Router);
    TestBed.createComponent(Stub).detectChanges();
  });

  /** Awaiting the navigation is what waits for the guard's observable to answer. */
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
      api.stores = [store('65f023632bc46470c104b76f', 'Acme Supply Co.')];
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

    api.stores = [store('65f023632bc46470c104b76f', 'Acme Supply Co.')];

    expect(await go('/dashboard')).toBe('/dashboard');
  });

  it('loads the store list, which is the other half of what these guards are for', async () => {
    // The request context reads this list synchronously on every later request, so a console route
    // must never activate before it has been fetched.
    expect(api.loads).toBe(0);
    await go('/dashboard');
    expect(api.loads).toBeGreaterThan(0);
  });

  /*
   * The platform half of the console. A super admin is not store-less — `InternalStoreServiceImpl`
   * hands them every tenant's stores — so these are about where they *belong*, not about whether
   * they are held. See lessons.md, "Shell — a super admin's store rail is the whole platform,
   * truncated".
   */
  describe('for a platform operator', () => {
    beforeEach(() => {
      permissions.platform = true;
    });

    it('is never held on getting-started, even with no store of their own', async () => {
      expect(await go('/getting-started')).toBe('/platform');
      expect(await go('/dashboard')).toBe('/platform');
    });

    it('sends getting-started to the platform rather than the merchant dashboard', async () => {
      api.stores = [store('65f023632bc46470c104b76f', 'Acme Supply Co.')];

      expect(await go('/getting-started')).toBe('/platform');
    });

    it('lets the platform pages through', async () => {
      expect(await go('/platform')).toBe('/platform');
      expect(await go('/platform/organizations')).toBe('/platform/organizations');
    });

    /*
     * The other half, and the one the user reported: a super admin's merchant pages were rendering
     * an access-denied banner. Every one is a reading of one store, and the store they are handed
     * belongs to another tenant — so there is nothing there for them to read.
     */
    it('is turned away from the merchant pages, even with a store in the list', async () => {
      api.stores = [store('65f023632bc46470c104b76f', 'Acme Supply Co.')];

      expect(await go('/catalogue')).toBe('/platform');
    });
  });

  /*
   * A merchant who types a platform URL gets the dashboard, not a page whose every request 403s.
   * The rail hides the group; this is what makes the URL unreachable too.
   */
  it('turns a merchant away from the platform pages', async () => {
    api.stores = [store('65f023632bc46470c104b76f', 'Acme Supply Co.')];

    expect(await go('/platform')).toBe('/dashboard');
    expect(await go('/platform/organizations')).toBe('/dashboard');
  });

  /*
   * The bug this case exists for, found in QA: `/platform` typed into the address bar bounced to the
   * dashboard, while the same link clicked in the rail worked.
   *
   * **A route's `canActivate` guards run concurrently.** Angular subscribes to all of them at once
   * through `prioritizedGuardValue` and only *reports* them in declaration order, so a guard that
   * reads what `canAccessSecuredPages` caches sees an empty principal on a cold load and a populated
   * one on every navigation after. Each guard has to resolve what it depends on.
   *
   * The fake mirrors that: `canAdministerPlatform()` answers `false` until `getAuthUser()` has been
   * subscribed, so a guard reading it synchronously fails here.
   */
  it('resolves the principal itself rather than relying on a sibling guard having run', async () => {
    /*
     * The account owns a store, so the redirect this is guarding against actually settles on the
     * dashboard. Without one, `requiresStore` bounces a platform operator straight back to
     * `/platform` and the case passes whether or not the bug is present — which it did on the first
     * attempt at writing it.
     */
    api.stores = [store('65f023632bc46470c104b76f', 'Acme Supply Co.')];
    permissions.platform = true;
    expect(permissions.canAdministerPlatform())
      .withContext('the fake must start cold, or this case cannot fail')
      .toBeFalse();

    expect(await go('/platform')).toBe('/platform');
  });

  it('leaves a merchant their own pages', async () => {
    api.stores = [store('65f023632bc46470c104b76f', 'Acme Supply Co.')];

    expect(await go('/catalogue')).toBe('/catalogue');
  });
});
