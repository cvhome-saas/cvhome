import {TestBed, fakeAsync, tick} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {of} from 'rxjs';

import type {StoreDirectory} from '@models/console';
import {translocoTesting} from '@testing/transloco-testing';
import {CONSOLE_STORES_FAKE, FakeConsoleApi} from '@testing/console-api.fake';
import {ConsoleShellFacade} from '../../facades/console-shell.facade';
import {ConsoleApi} from '../../services/console.api.service';
import {StoreSwitcher} from './store-switcher';

describe('StoreSwitcher', () => {
  beforeEach(() => {
    // Selection is persisted, so a case that switches stores must not leak into the next.
    localStorage.removeItem('cvhome.console.store');
    TestBed.configureTestingModule({
      imports: [...translocoTesting().imports],
      providers: [
        provideRouter([]),
        ...translocoTesting().providers,
        {provide: ConsoleApi, useValue: Object.assign(new FakeConsoleApi(), {stores: CONSOLE_STORES_FAKE})},
      ],
    });
  });

  /** The list arrives asynchronously, so every case has to let the load settle first. */
  function switcher() {
    const fixture = TestBed.createComponent(StoreSwitcher);
    const facade = TestBed.inject(ConsoleShellFacade);
    fixture.detectChanges();
    tick(500);
    fixture.detectChanges();
    return {fixture, facade, element: fixture.nativeElement as HTMLElement};
  }

  function names(element: HTMLElement): string[] {
    return [...element.querySelectorAll('.store-name')].map((node) => node.textContent!.trim());
  }

  it('renders the stores the API returns, not a hardcoded list', fakeAsync(() => {
    const {element, facade} = switcher();

    expect(names(element)).toEqual(facade.stores().map((store) => store.name));
    expect(element.querySelector('.store.current .store-name')?.textContent?.trim()).toBe(
      facade.currentStore()!.name,
    );
  }));

  it('shows a placeholder until the stores load', fakeAsync(() => {
    const fixture = TestBed.createComponent(StoreSwitcher);
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('.store-placeholder')).not.toBeNull();

    tick(500);
    fixture.detectChanges();
    expect(element.querySelector('.store-placeholder')).toBeNull();
  }));

  it('opens the store that is clicked', fakeAsync(() => {
    const {fixture, facade, element} = switcher();
    const second = facade.stores()[1];

    (element.querySelectorAll('.store')[1] as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(facade.currentStoreId()).toBe(second.id);
    expect(element.querySelector('.store.current .store-name')?.textContent?.trim()).toBe(second.name);
  }));

  it('marks a store that is still building, and one that failed', fakeAsync(() => {
    const api = TestBed.inject(ConsoleApi);
    spyOn(api, 'loadStores').and.returnValue(
      of({
        stores: [
          {id: 'a', name: 'Building', provisioningState: 'IN_PROGRESS_PROVISIONING', status: 'ACTIVE'},
          {id: 'b', name: 'Broken', provisioningState: 'FAILED_PROVISIONING', status: 'ACTIVE'},
        ],
        currentStoreId: 'a',
      } satisfies StoreDirectory),
    );

    const {element} = switcher();
    const rows = [...element.querySelectorAll('.store-list li')];

    expect(rows.length).toBe(2);
    expect(rows[0].querySelector('.store-flags app-icon')).not.toBeNull();
    expect(rows[1].querySelector('.store-flags app-icon')).not.toBeNull();
  }));

  it('offers no pin or reorder controls — neither has anywhere to be saved', fakeAsync(() => {
    const {element} = switcher();

    expect(element.querySelector('.store-menu')).toBeNull();
    expect(element.querySelector('.store-move')).toBeNull();
  }));
});
