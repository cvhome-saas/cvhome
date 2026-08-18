import {TestBed, fakeAsync, tick} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {throwError} from 'rxjs';

import {translocoTesting} from '@testing/transloco-testing';
import {ConsoleShellFacade} from '../../facades/console-shell.facade';
import {ConsoleApi} from '../../services/console.api.service';
import {StoreSwitcher} from './store-switcher';

describe('StoreSwitcher', () => {
  beforeEach(() => {
    // Selection is persisted, so a case that switches stores must not leak into the next.
    localStorage.removeItem('cvhome.console.store');
    TestBed.configureTestingModule({
      imports: [...translocoTesting().imports],
      providers: [provideRouter([]), ...translocoTesting().providers],
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

  it('pins the open store as the default, once the save lands', fakeAsync(() => {
    const {fixture, facade, element} = switcher();
    facade.selectStore(facade.stores()[2].id);
    facade.toggleMenu('store');
    fixture.detectChanges();

    (element.querySelector('.store-menu button') as HTMLButtonElement).click();
    fixture.detectChanges();
    // Nothing moves until the API confirms.
    expect(facade.defaultStoreId()).toBe(facade.stores()[0].id);

    tick(500);
    fixture.detectChanges();
    expect(facade.defaultStore()).toBe(facade.stores()[2].name);
    expect(element.querySelectorAll('.store')[2].querySelector('app-icon[name="pin"]')).not.toBeNull();
  }));

  it('moves a store down the rail and saves the order', fakeAsync(() => {
    const {fixture, facade, element} = switcher();
    const api = TestBed.inject(ConsoleApi);
    spyOn(api, 'reorderStores').and.callThrough();
    const [first, second] = facade.stores();

    facade.toggleReorder();
    fixture.detectChanges();
    (element.querySelectorAll('.store-move button')[1] as HTMLButtonElement).click();
    fixture.detectChanges();

    // Optimistic: the row moves before the save returns.
    expect(names(element).slice(0, 2)).toEqual([second.name, first.name]);
    expect(api.reorderStores).toHaveBeenCalledWith([second.id, first.id, facade.stores()[2].id]);
    tick(500);
  }));

  it('puts the order back if the save fails', fakeAsync(() => {
    const {fixture, facade, element} = switcher();
    const api = TestBed.inject(ConsoleApi);
    const before = names(element);
    spyOn(api, 'reorderStores').and.returnValue(throwError(() => new Error('save failed')));

    facade.moveStore(facade.stores()[0].id, 1);
    fixture.detectChanges();

    expect(names(element)).toEqual(before);
  }));

  it('offers move controls only while reordering, and stops at the ends', fakeAsync(() => {
    const {fixture, facade, element} = switcher();
    expect(element.querySelectorAll('.store-move').length).toBe(0);

    facade.toggleReorder();
    fixture.detectChanges();

    const moves = element.querySelectorAll<HTMLButtonElement>('.store-move button');
    expect(moves.length).toBe(facade.stores().length * 2);
    // First store cannot move up, last cannot move down.
    expect(moves[0].disabled).toBeTrue();
    expect(moves[moves.length - 1].disabled).toBeTrue();
  }));
});
