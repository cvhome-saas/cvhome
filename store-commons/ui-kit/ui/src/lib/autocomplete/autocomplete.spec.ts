import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';

import {kitTranslocoTesting} from '@cvhome-saas/ui-kit/i18n';
import {Autocomplete, type AutocompleteOption} from './autocomplete';

const RESULTS: readonly AutocompleteOption[] = [
  {id: 1, label: 'Wireless headphones', detail: 'ACM-1'},
  {id: 2, label: 'Wired headphones', detail: 'ACM-2'},
  {id: 3, label: 'Desk chair', detail: 'ACM-3'},
];

@Component({
  imports: [Autocomplete],
  template: `
    <app-autocomplete
      [options]="options()"
      [loading]="loading()"
      label="Add a product"
      placeholder="Search products…"
      (termChanged)="terms.push($event)"
      (selected)="picked.push($event.id)"
    />
  `,
})
class Host {
  readonly options = signal<readonly AutocompleteOption[]>([]);
  readonly loading = signal(false);
  readonly terms: string[] = [];
  readonly picked: number[] = [];
}

describe('Autocomplete', () => {
  let fixture: ComponentFixture<Host>;
  let host: Host;
  let element: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Host, ...kitTranslocoTesting().imports],
      providers: [...kitTranslocoTesting().providers],
    }).compileComponents();

    fixture = TestBed.createComponent(Host);
    host = fixture.componentInstance;
    fixture.detectChanges();
    element = fixture.nativeElement as HTMLElement;
  });

  function input(): HTMLInputElement {
    return element.querySelector('input')!;
  }

  function type(value: string): void {
    input().value = value;
    input().dispatchEvent(new Event('input'));
    fixture.detectChanges();
  }

  function press(key: string): KeyboardEvent {
    const event = new KeyboardEvent('keydown', {key, bubbles: true, cancelable: true});
    input().dispatchEvent(event);
    fixture.detectChanges();
    return event;
  }

  function optionRows(): HTMLElement[] {
    return [...element.querySelectorAll<HTMLElement>('.option:not(.empty)')];
  }

  it('waits for the pause before asking, and asks once', fakeAsync(() => {
    type('he');
    type('hea');
    type('head');

    // Nothing yet: three keystrokes inside the window are one search, not three.
    expect(host.terms).toEqual([]);

    tick(250);
    expect(host.terms).toEqual(['head']);
  }));

  it('does not search on a fragment too short to mean anything', fakeAsync(() => {
    type('h');
    tick(500);

    expect(host.terms).toEqual([]);
    // And it stays shut, so there is no "no matches" for a search that never happened.
    expect(element.querySelector('.options')).toBeNull();
  }));

  it('trims before searching, so a trailing space is not a different query', fakeAsync(() => {
    type('  chair  ');
    tick(250);

    expect(host.terms).toEqual(['chair']);
  }));

  it('says it is searching, then that there is nothing, and never confuses the two', fakeAsync(() => {
    host.loading.set(true);
    type('head');
    tick(250);
    fixture.detectChanges();

    expect(element.querySelector('.option.empty')?.textContent).toContain('Searching');

    host.loading.set(false);
    host.options.set([]);
    fixture.detectChanges();

    expect(element.querySelector('.option.empty')?.textContent).toContain('No matches');
  }));

  it('walks the results with the arrows and wraps at both ends', fakeAsync(() => {
    type('head');
    tick(250);
    host.options.set(RESULTS);
    fixture.detectChanges();

    press('ArrowDown');
    expect(optionRows()[0].classList).toContain('active');

    press('ArrowDown');
    press('ArrowDown');
    expect(optionRows()[2].classList).toContain('active');

    // Wraps rather than stopping: the list has no dead end.
    press('ArrowDown');
    expect(optionRows()[0].classList).toContain('active');

    press('ArrowUp');
    expect(optionRows()[2].classList).toContain('active');
  }));

  it('points aria-activedescendant at the highlighted row, so the reading position follows', fakeAsync(() => {
    type('head');
    tick(250);
    host.options.set(RESULTS);
    fixture.detectChanges();

    press('ArrowDown');
    const active = element.querySelector('.option.active')!;
    expect(input().getAttribute('aria-activedescendant')).toBe(active.id);
  }));

  it('clears the highlight when the results change under it', fakeAsync(() => {
    type('head');
    tick(250);
    host.options.set(RESULTS);
    fixture.detectChanges();
    press('ArrowDown');
    press('ArrowDown');

    // A new search: pointing at an id that is no longer in the list reads as silence.
    host.options.set([{id: 9, label: 'Something else'}]);
    fixture.detectChanges();

    const descendant = input().getAttribute('aria-activedescendant');
    expect(descendant).toContain('-9');
  }));

  it('picks with Enter and clears itself for the next search', fakeAsync(() => {
    type('head');
    tick(250);
    host.options.set(RESULTS);
    fixture.detectChanges();

    press('ArrowDown');
    press('Enter');

    expect(host.picked).toEqual([1]);
    // Cleared, not filled with the label: this picker adds to a list, so the next thing typed is a
    // new search rather than an edit of the last one.
    expect(input().value).toBe('');
    expect(element.querySelector('.options')).toBeNull();
  }));

  it('leaves Enter alone when nothing is highlighted, so a form can still submit', fakeAsync(() => {
    type('head');
    tick(250);
    host.options.set(RESULTS);
    fixture.detectChanges();

    const event = press('Enter');

    expect(host.picked).toEqual([]);
    expect(event.defaultPrevented).toBe(false);
  }));

  it('closes on Escape without picking', fakeAsync(() => {
    type('head');
    tick(250);
    host.options.set(RESULTS);
    fixture.detectChanges();

    press('Escape');

    expect(element.querySelector('.options')).toBeNull();
    expect(host.picked).toEqual([]);
  }));

  it('picks on a pointer press, before blur can close the list', fakeAsync(() => {
    type('head');
    tick(250);
    host.options.set(RESULTS);
    fixture.detectChanges();

    // `mousedown`, not `click`: the list is gone by the time a click would land.
    optionRows()[1].dispatchEvent(new MouseEvent('mousedown', {bubbles: true, cancelable: true}));
    fixture.detectChanges();

    expect(host.picked).toEqual([2]);
  }));

  it('stays open long enough after blur for a pointer to finish its click', fakeAsync(() => {
    type('head');
    tick(250);
    host.options.set(RESULTS);
    fixture.detectChanges();

    input().dispatchEvent(new Event('blur'));
    fixture.detectChanges();
    expect(element.querySelector('.options')).not.toBeNull();

    tick(200);
    fixture.detectChanges();
    expect(element.querySelector('.options')).toBeNull();
  }));
});
