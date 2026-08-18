import {TestBed, fakeAsync, tick} from '@angular/core/testing';
import {provideRouter} from '@angular/router';

import {translocoTesting} from '@testing/transloco-testing';
import {FakeConsoleApi, consoleStore} from '@testing/console-api.fake';
import {ConsoleShell} from './console-shell';
import {ConsoleShellFacade} from './facades/console-shell.facade';
import {ConsoleApi} from './services/console.api.service';

/**
 * The shell in first run: an account with no store.
 *
 * Separate from `console-shell.spec.ts` because these cases need the store directory to
 * come back empty, and that has to be decided before the root facade is constructed.
 */
describe('ConsoleShell (first run)', () => {
  let api: FakeConsoleApi;

  beforeEach(async () => {
    localStorage.removeItem('cvhome.console.store');
    api = new FakeConsoleApi();
    await TestBed.configureTestingModule({
      imports: [ConsoleShell, ...translocoTesting().imports],
      providers: [
        provideRouter([]),
        {provide: ConsoleApi, useValue: api},
        ...translocoTesting().providers,
      ],
    }).compileComponents();
  });

  function shell() {
    const fixture = TestBed.createComponent(ConsoleShell);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    return {fixture, element: fixture.nativeElement as HTMLElement};
  }

  it('reports first run only once the empty directory has actually answered', fakeAsync(() => {
    const fixture = TestBed.createComponent(ConsoleShell);
    const facade = TestBed.inject(ConsoleShellFacade);
    fixture.detectChanges();

    // Mid-request, an empty list is absence of an answer — not an answer.
    expect(facade.storesLoading()).toBeTrue();
    expect(facade.firstRun()).toBeFalse();

    tick();
    fixture.detectChanges();

    expect(facade.firstRun()).toBeTrue();
  }));

  it('disables every navigation item, and says why', fakeAsync(() => {
    const {element} = shell();

    // Nothing in the rail routes anywhere while there is no store to route into.
    expect(element.querySelectorAll('a.nav-item').length).toBe(0);

    const items = [...element.querySelectorAll('button.nav-item')] as HTMLButtonElement[];
    expect(items.length).toBeGreaterThan(0);
    for (const item of items) {
      expect(item.disabled).toBeTrue();
      expect(item.getAttribute('aria-disabled')).toBe('true');
      // A disabled control that explains nothing is just a broken one.
      expect(item.title).toContain('first store');
    }
  }));

  it('keeps a disabled item out of the keyboard tab order', fakeAsync(() => {
    const {element} = shell();
    const item = element.querySelector('button.nav-item') as HTMLButtonElement;

    item.focus();

    expect(document.activeElement).not.toBe(item);
  }));

  it('names the missing store instead of showing an empty list', fakeAsync(() => {
    const {element} = shell();

    expect(element.querySelectorAll('.store-list li').length).toBe(0);
    expect(element.querySelector('.store-empty')?.textContent).toContain('No store yet');
  }));

  it('keeps store creation reachable from the rail — it is the only way out', fakeAsync(() => {
    const {element} = shell();
    const create = element.querySelector('a.create-store') as HTMLAnchorElement;

    expect(create).not.toBeNull();
    expect(create.getAttribute('href')).toBe('/store-management/create');
  }));

  it('suppresses the plan strip, so the page owns that slot', fakeAsync(() => {
    const {fixture, element} = shell();
    const facade = TestBed.inject(ConsoleShellFacade);

    expect(facade.bannerVisible()).toBeTrue();
    expect(facade.bannerShown()).toBeFalse();
    expect(element.querySelector('app-plan-banner')).toBeNull();
    // The reserved height has to agree with the strip's absence, or the layout gaps.
    expect(element.querySelector('.console')?.classList).not.toContain('banner-on');

    api.stores = [consoleStore('store-1', 'Acme Supply Co.')];
    facade.refreshStores();
    tick();
    fixture.detectChanges();

    expect(element.querySelector('app-plan-banner')).not.toBeNull();
  }));

  describe('once a store is created', () => {
    it('re-enables the rail and opens the new store', fakeAsync(() => {
      const {fixture, element} = shell();
      const facade = TestBed.inject(ConsoleShellFacade);
      expect(facade.firstRun()).toBeTrue();

      api.stores = [consoleStore('store-1', 'Acme Supply Co.')];
      facade.refreshStores();
      tick();
      fixture.detectChanges();

      expect(facade.firstRun()).toBeFalse();
      expect(facade.currentStore()?.name).toBe('Acme Supply Co.');
      expect(element.querySelectorAll('a.nav-item').length).toBeGreaterThan(0);
      expect(element.querySelector('.store-empty')).toBeNull();
    }));
  });
});
