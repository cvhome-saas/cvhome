import {TestBed} from '@angular/core/testing';
import {Observable, of, throwError} from 'rxjs';

import {NOTIFICATION_PORT} from '@cvhome-saas/ui-kit';
import {kitTranslocoTesting} from '@cvhome-saas/ui-kit/i18n';
import type {PlatformUserRow, SessionSummary} from '@cvhome-saas/ui-kit/uaa';
import {ShoppersApi, type ShoppersQuery, type ShoppersSnapshot} from '../services/shoppers.api.service';
import {ShoppersFacade} from './shoppers.facade';

const ROW: PlatformUserRow = {
  id: 'u-1',
  username: 'mia',
  email: 'mia@example.com',
  name: 'Mia Example',
  roles: [],
  enabled: true,
  status: 'ACTIVE',
  lastSignInAt: null,
  org: null,
  store: null,
  initials: 'ME',
};

class FakeApi {
  readonly queries: ShoppersQuery[] = [];
  readonly disabled: string[] = [];
  sessionsFail = false;

  load(query: ShoppersQuery): Observable<ShoppersSnapshot> {
    this.queries.push(query);
    return of({rows: [ROW], totalElements: 1, totalPages: 1});
  }

  disable(id: string): Observable<void> {
    this.disabled.push(id);
    return of(undefined);
  }

  enable(): Observable<void> {
    return of(undefined);
  }

  unlock(): Observable<void> {
    return of(undefined);
  }

  sessions(): Observable<readonly SessionSummary[]> {
    return this.sessionsFail ? throwError(() => new Error('nope')) : of([]);
  }

  revokeSessions(): Observable<{revoked: number}> {
    return of({revoked: 2});
  }

  delete(): Observable<void> {
    return of(undefined);
  }
}

describe('ShoppersFacade', () => {
  let api: FakeApi;
  let facade: ShoppersFacade;

  beforeEach(() => {
    api = new FakeApi();
    const transloco = kitTranslocoTesting();
    TestBed.configureTestingModule({
      imports: [...(transloco.imports as never[])],
      providers: [
        ...transloco.providers,
        {provide: NOTIFICATION_PORT, useValue: {danger: () => undefined, success: () => undefined}},
        {provide: ShoppersApi, useValue: api},
        ShoppersFacade,
      ],
    });
    facade = TestBed.inject(ShoppersFacade);
    TestBed.tick();
  });

  /** Page 4 of a narrower result is nothing, so a filter change has to start over. */
  it('returns to the first page when a filter changes', () => {
    facade.setPage(3);
    expect(facade.pageIndex()).toBe(3);

    facade.setQuery('mia');
    TestBed.tick();

    expect(facade.pageIndex()).toBe(0);
    expect(api.queries.at(-1)?.q).toBe('mia');
  });

  /** An empty list means two different things, and the page says which. */
  it('knows an empty result under a filter from an empty store', () => {
    expect(facade.filtered()).toBeFalse();

    facade.setStatus('LOCKED');

    expect(facade.filtered()).toBeTrue();
  });

  it('disables an enabled shopper rather than enabling it again', () => {
    facade.toggleEnabled(ROW);

    expect(api.disabled).toEqual(['u-1']);
  });

  /**
   * The merchant asked to see where an account is signed in. An empty list that actually means
   * "could not load" is a lie, so the failure is carried into the pane.
   */
  it('surfaces a sessions failure in the pane instead of an empty list', () => {
    api.sessionsFail = true;

    facade.inspect(ROW);

    expect(facade.inspecting()).toBe(ROW);
    expect(facade.sessionsFailed()).toBeTrue();
    expect(facade.busy()).toBeFalse();
  });

  it('closing the pane forgets what it held', () => {
    facade.inspect(ROW);
    facade.dismissDialogs();

    expect(facade.inspecting()).toBeNull();
    expect(facade.sessions()).toEqual([]);
    expect(facade.sessionsFailed()).toBeFalse();
  });
});
