import {TestBed, discardPeriodicTasks, fakeAsync, tick} from '@angular/core/testing';
import {provideRouter} from '@angular/router';

import {translocoTesting} from '@testing/transloco-testing';
import {CONSOLE_STORES_FAKE, FakeConsoleApi} from '@testing/console-api.fake';
import {ImpersonationBanner} from './impersonation-banner';
import {ConsoleApi} from '../../services/console.api.service';

/**
 * The banner reads the gateway's word for it and carries the only control that ends it. Driven, not
 * asserted for presence: the end button must reach the api, and the store id must become a name where
 * the rail knows one.
 */
describe('ImpersonationBanner', () => {
  let api: FakeConsoleApi;

  beforeEach(async () => {
    api = Object.assign(new FakeConsoleApi(), {stores: CONSOLE_STORES_FAKE, platformOperator: false});
    api.impersonation = {
      actingAs: 'org1-store1-admin',
      targetId: 't',
      storeId: CONSOLE_STORES_FAKE[0].id,
      mode: 'read',
      reason: 'ticket 42',
      expiresAt: new Date(Date.now() + 10 * 60_000).toISOString(),
    };
    await TestBed.configureTestingModule({
      imports: [ImpersonationBanner, ...translocoTesting().imports],
      providers: [provideRouter([]), ...translocoTesting().providers, {provide: ConsoleApi, useValue: api}],
    }).compileComponents();
  });

  /**
   * The facade is root-provided, so its `rxResource` effect flushes on the application tick rather than
   * on the fixture's — `TestBed.tick()` is what settles it. Inside `fakeAsync`, so the countdown's interval
   * can be discarded rather than left running past the spec.
   */
  function banner() {
    const fixture = TestBed.createComponent(ImpersonationBanner);
    fixture.detectChanges();
    tick();
    TestBed.tick();
    fixture.detectChanges();
    return {fixture, element: fixture.nativeElement as HTMLElement};
  }

  it('names the merchant, the store and the mode, and ends it through the api', fakeAsync(() => {
    const {fixture, element} = banner();

    const text = element.textContent ?? '';
    expect(text).toContain('org1-store1-admin');
    expect(text).toContain(CONSOLE_STORES_FAKE[0].name);
    expect(text).toContain('Read-only');
    expect(element.querySelector('.dismiss')).toBeNull();

    (element.querySelector('.end') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(api.ended).toBe(1);
    expect(api.reloadedTo).toBe('/platform/users');
    discardPeriodicTasks();
  }));

  it('renders nothing when the session is itself', fakeAsync(() => {
    api.impersonation = null;

    const {element} = banner();

    expect(element.querySelector('.impersonation-banner')).toBeNull();
    discardPeriodicTasks();
  }));
});
