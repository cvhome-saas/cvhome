import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';

import type {ReferenceOption} from '@cvhome-saas/ui-kit';
import {kitTranslocoTesting} from '@cvhome-saas/ui-kit/i18n';
import {LocaleSwitcher} from './locale-switcher';

const LANGUAGES: readonly ReferenceOption[] = [
  {code: 'en', label: 'English'},
  {code: 'ar', label: 'Arabic'},
  {code: 'fr', label: 'French'},
];

@Component({
  imports: [LocaleSwitcher],
  template: `
    <app-locale-switcher
      [languages]="languages"
      [(active)]="active"
      [filled]="filled()"
      label="Editing language"
    />
  `,
})
class Host {
  readonly languages = LANGUAGES;
  readonly active = signal('en');
  readonly filled = signal<ReadonlySet<string>>(new Set(['en']));
}

describe('LocaleSwitcher', () => {
  let fixture: ComponentFixture<Host>;
  let host: Host;
  let element: HTMLElement;

  const chips = () => Array.from(element.querySelectorAll('.chip')) as HTMLButtonElement[];
  const group = () => element.querySelector('[role="radiogroup"]') as HTMLElement;
  const press = (key: string) => {
    group().dispatchEvent(new KeyboardEvent('keydown', {key, bubbles: true}));
    fixture.detectChanges();
  };

  beforeEach(async () => {
    const transloco = kitTranslocoTesting();
    await TestBed.configureTestingModule({
      imports: [Host, ...(transloco.imports as never[])],
      providers: transloco.providers,
    }).compileComponents();
    fixture = TestBed.createComponent(Host);
    host = fixture.componentInstance;
    fixture.detectChanges();
    element = fixture.nativeElement as HTMLElement;
  });

  it('is one radio group, not a tablist — the languages are values of a field', () => {
    expect(group().getAttribute('aria-label')).toBe('Editing language');
    expect(chips().map((chip) => chip.getAttribute('role'))).toEqual(['radio', 'radio', 'radio']);
  });

  it('marks the current language and keeps the strip to one tab stop', () => {
    expect(chips().map((chip) => chip.getAttribute('aria-checked'))).toEqual(['true', 'false', 'false']);
    expect(chips().map((chip) => chip.getAttribute('tabindex'))).toEqual(['0', '-1', '-1']);
  });

  it('says which languages already hold copy, in words as well as a mark', () => {
    expect(chips()[0].title).toBe('Translated in English');
    expect(chips()[1].title).toBe('Not translated in Arabic');
    expect(chips()[0].querySelector('app-icon')).not.toBeNull();
    expect(chips()[1].querySelector('app-icon')).toBeNull();
  });

  it('chooses on click', () => {
    chips()[2].click();
    fixture.detectChanges();
    expect(host.active()).toBe('fr');
  });

  /*
   * The behaviour this component gained on being promoted. Its catalogue original set a roving
   * tabindex and left a comment claiming arrows came "for free" — they do not: `role="radio"` on a
   * <button> buys the announcement and none of the interaction. The strip had one tab stop and no
   * keyboard route to the other languages at all.
   */
  describe('keyboard', () => {
    it('moves and selects with the arrows', () => {
      press('ArrowRight');
      expect(host.active()).toBe('ar');
      press('ArrowDown');
      expect(host.active()).toBe('fr');
      press('ArrowLeft');
      expect(host.active()).toBe('ar');
    });

    it('wraps at both ends', () => {
      press('ArrowLeft');
      expect(host.active()).toBe('fr');
      press('ArrowRight');
      expect(host.active()).toBe('en');
    });

    it('jumps to the ends with Home and End', () => {
      press('End');
      expect(host.active()).toBe('fr');
      press('Home');
      expect(host.active()).toBe('en');
    });

    it('ignores keys that are not navigation', () => {
      press('a');
      expect(host.active()).toBe('en');
    });

    it('moves the tab stop with the selection', () => {
      press('ArrowRight');
      expect(chips().map((chip) => chip.getAttribute('tabindex'))).toEqual(['-1', '0', '-1']);
    });
  });
});
