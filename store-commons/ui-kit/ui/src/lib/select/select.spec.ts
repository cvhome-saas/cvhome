import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {FormControl, ReactiveFormsModule} from '@angular/forms';

import {Select, type SelectOption} from './select';

const OPTIONS: readonly SelectOption[] = [
  {value: '', label: 'No brand'},
  {value: 'NIKE', label: 'Nike'},
  {value: 'ADIDAS', label: 'Adidas'},
  {value: 'ZARA', label: 'Zara', disabled: true},
  {value: 'ZALANDO', label: 'Zalando'},
];

@Component({
  imports: [Select, ReactiveFormsModule],
  template: `
    <div [attr.dir]="dir()">
      <app-select [formControl]="control" [options]="options()" ariaLabel="Brand" placeholder="Pick one" />
    </div>
  `,
})
class Host {
  readonly control = new FormControl('', {nonNullable: true});
  readonly options = signal<readonly SelectOption[]>(OPTIONS);
  readonly dir = signal('ltr');
}

describe('Select', () => {
  let fixture: ComponentFixture<Host>;
  let host: Host;
  let element: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({imports: [Host]}).compileComponents();
    fixture = TestBed.createComponent(Host);
    host = fixture.componentInstance;
    fixture.detectChanges();
    element = fixture.nativeElement as HTMLElement;
  });

  function trigger(): HTMLButtonElement {
    return element.querySelector<HTMLButtonElement>('.select-trigger')!;
  }

  function options(): HTMLButtonElement[] {
    return [...element.querySelectorAll<HTMLButtonElement>('.select-option')];
  }

  /** A keystroke on the component, wherever focus currently is inside it. */
  function press(key: string): void {
    const target = (document.activeElement as HTMLElement) ?? trigger();
    target.dispatchEvent(new KeyboardEvent('keydown', {key, bubbles: true}));
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
  }

  function open(): void {
    trigger().focus();
    press('ArrowDown');
  }

  /* ---------------------------------------------------------------------- closed ---- */

  it('shows the placeholder only when no option claims the empty value', () => {
    /*
     * `No brand` is a real choice with a real value, so it wins — a placeholder is for "nothing
     * chosen yet", which is a different state from "chosen: none". Every consumer in the catalogue
     * offers the explicit option, which is why the placeholder rarely shows at all.
     */
    expect(trigger().textContent).toContain('No brand');
    expect(trigger().classList).not.toContain('is-placeholder');

    host.options.set(OPTIONS.filter((option) => option.value !== ''));
    fixture.detectChanges();

    expect(trigger().textContent).toContain('Pick one');
    expect(trigger().classList).toContain('is-placeholder');
  });

  it('adds nothing to the accessibility tree while it is closed', () => {
    // An idle picker should not announce a listbox that is not there.
    expect(element.querySelector('[role="listbox"]')).toBeNull();
    expect(trigger().getAttribute('aria-expanded')).toBe('false');
  });

  it('is one tab stop, whatever the list holds', fakeAsync(() => {
    open();

    const focusable = [...element.querySelectorAll<HTMLElement>('[tabindex]')].filter(
      (node) => node.tabIndex === 0,
    );
    // The trigger has no explicit tabindex; the options carry a roving one, and only one is 0.
    expect(focusable.length).toBe(1);
    expect(focusable[0].classList).toContain('select-option');
  }));

  /* ------------------------------------------------------------------- choosing ---- */

  it('writes the chosen value back through the form', fakeAsync(() => {
    open();
    press('ArrowDown');
    press('Enter');

    expect(host.control.value).toBe('NIKE');
    expect(trigger().textContent).toContain('Nike');
  }));

  it('takes a value the form gives it', () => {
    host.control.setValue('ADIDAS');
    fixture.detectChanges();

    expect(trigger().textContent).toContain('Adidas');
    expect(trigger().classList).not.toContain('is-placeholder');
  });

  it('opens onto the current value rather than the top of the list', fakeAsync(() => {
    host.control.setValue('ADIDAS');
    fixture.detectChanges();
    open();

    expect(options()[2].getAttribute('aria-selected')).toBe('true');
    expect(options()[2].tabIndex).toBe(0);
  }));

  it('abandons on Escape, keeping the value the field had', fakeAsync(() => {
    host.control.setValue('NIKE');
    fixture.detectChanges();
    open();
    press('ArrowDown');
    press('Escape');

    expect(element.querySelector('[role="listbox"]')).toBeNull();
    expect(host.control.value).toBe('NIKE');
  }));

  it('marks the control touched when the list closes', fakeAsync(() => {
    expect(host.control.touched).toBe(false);
    open();
    press('Escape');

    expect(host.control.touched).toBe(true);
  }));

  /* ------------------------------------------------------------------- keyboard ---- */

  it('steps over an unavailable option instead of landing on it', fakeAsync(() => {
    open();
    // '' → Nike → Adidas → (Zara is disabled) → Zalando
    press('ArrowDown');
    press('ArrowDown');
    press('ArrowDown');
    press('Enter');

    expect(host.control.value).toBe('ZALANDO');
  }));

  it('wraps at both ends', fakeAsync(() => {
    open();
    press('ArrowUp');
    press('Enter');

    // Up from the first lands on the last, not on nothing.
    expect(host.control.value).toBe('ZALANDO');
  }));

  it('goes to the ends with Home and End', fakeAsync(() => {
    open();
    press('End');
    press('Enter');
    expect(host.control.value).toBe('ZALANDO');

    open();
    press('Home');
    press('Enter');
    expect(host.control.value).toBe('');
  }));

  it('finds an option by typing its first letters', fakeAsync(() => {
    open();
    press('a');

    expect(options()[2].tabIndex).toBe(0);
    press('Enter');
    expect(host.control.value).toBe('ADIDAS');
  }));

  it('cycles through the options sharing a letter when it is pressed again', fakeAsync(() => {
    /*
     * What a native select does, and what anyone who has used one expects: `z` twice moves from Zara
     * to Zalando rather than sticking on the first match.
     */
    open();
    press('z');
    // Zara is disabled, so the first `z` already lands on Zalando.
    expect(options()[4].tabIndex).toBe(0);
  }));

  it('opens onto a match when a letter is typed while closed', fakeAsync(() => {
    trigger().focus();
    // Two letters, because `No brand` also begins with an n — the buffer is what disambiguates.
    press('n');
    press('i');

    expect(element.querySelector('[role="listbox"]')).not.toBeNull();
    expect(options()[1].tabIndex).toBe(0);
  }));

  it('leaves the field on Tab without choosing', fakeAsync(() => {
    open();
    press('ArrowDown');
    press('Tab');

    expect(element.querySelector('[role="listbox"]')).toBeNull();
    expect(host.control.value).toBe('');
  }));

  it('opens upward when there is no room below', fakeAsync(() => {
    /*
     * The dimension-unit select sits at the foot of the pricing step, so a list that only ever drops
     * downward opened past the end of the page and its last options could not be reached at all.
     */
    trigger().style.position = 'fixed';
    trigger().style.insetBlockEnd = '4px';
    trigger().style.insetBlockStart = 'auto';
    fixture.detectChanges();

    open();

    expect(element.querySelector('.select-list')?.classList).toContain('drop-up');
  }));

  it('opens downward when there is room, which is the ordinary case', fakeAsync(() => {
    trigger().style.position = 'fixed';
    trigger().style.insetBlockStart = '4px';
    fixture.detectChanges();

    open();

    expect(element.querySelector('.select-list')?.classList).not.toContain('drop-up');
  }));

  /* ---------------------------------------------------------------------- ARIA ---- */

  it('points aria-activedescendant at the option focus is on, and clears it when closed', fakeAsync(() => {
    open();
    press('ArrowDown');

    const active = trigger().getAttribute('aria-activedescendant');
    expect(active).toBe(options()[1].id);

    press('Escape');
    expect(trigger().getAttribute('aria-activedescendant')).toBeNull();
  }));

  it('marks an unavailable option with aria-disabled, not disabled', fakeAsync(() => {
    /*
     * A `disabled` button is skipped by screen-reader item counts, so an unavailable option would
     * silently vanish from a list of five and the operator could not tell whether it was
     * unavailable or never there.
     */
    open();

    expect(options()[3].getAttribute('aria-disabled')).toBe('true');
    expect(options()[3].disabled).toBe(false);
  }));

  it('refuses an unavailable option when it is clicked anyway', fakeAsync(() => {
    open();
    options()[3].click();
    fixture.detectChanges();

    expect(host.control.value).toBe('');
  }));

  /* ------------------------------------------------------------------ disabled ---- */

  it('cannot be opened once the form disables it', fakeAsync(() => {
    host.control.disable();
    fixture.detectChanges();

    expect(trigger().disabled).toBe(true);
    trigger().click();
    fixture.detectChanges();
    expect(element.querySelector('[role="listbox"]')).toBeNull();
  }));
});
