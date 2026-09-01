import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, ReactiveFormsModule, Validators} from '@angular/forms';

import {NumberField} from './number-field';

@Component({
  imports: [NumberField, ReactiveFormsModule],
  template: `
    <app-number-field
      [formControl]="control"
      [prefix]="prefix()"
      [suffix]="suffix()"
      [decimals]="decimals()"
      ariaLabel="Price"
      placeholder="0.00"
    />
  `,
})
class Host {
  readonly control = new FormControl<number | null>(null);
  readonly prefix = signal<string | null>(null);
  readonly suffix = signal<string | null>(null);
  readonly decimals = signal<number | null>(null);
}

describe('NumberField', () => {
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

  function input(): HTMLInputElement {
    return element.querySelector<HTMLInputElement>('.number-input')!;
  }

  function type(text: string): void {
    input().value = text;
    input().dispatchEvent(new Event('input', {bubbles: true}));
    fixture.detectChanges();
  }

  function blur(): void {
    input().dispatchEvent(new Event('blur'));
    fixture.detectChanges();
  }

  /* ------------------------------------------------------------------ the control ---- */

  it('is not a native number input', () => {
    /*
     * The whole reason the component exists. `type="number"` brings spin buttons nothing else in
     * this console has, edits itself on the scroll wheel, and reports an empty value for content it
     * cannot parse — so the form goes null while the operator is still looking at their digits.
     */
    expect(input().type).toBe('text');
    expect(input().getAttribute('inputmode')).toBe('decimal');
  });

  it('reads left to right whatever the locale', () => {
    // A price inside the Arabic console that flips is a price nobody can read.
    expect(input().getAttribute('dir')).toBe('ltr');
  });

  it('asks for the numeric keypad when the field takes no decimals', () => {
    host.decimals.set(0);
    fixture.detectChanges();

    expect(input().getAttribute('inputmode')).toBe('numeric');
  });

  /* ------------------------------------------------------------------- the value ---- */

  it('publishes what was typed, as a number', () => {
    type('129.5');

    expect(host.control.value).toBe(129.5);
    expect(typeof host.control.value).toBe('number');
  });

  it('shows what the form gave it', () => {
    host.control.setValue(42);
    fixture.detectChanges();

    expect(input().value).toBe('42');
  });

  it('keeps an empty field as null, never as zero', () => {
    /*
     * Load-bearing. An empty price means "not priced yet", which is a real state for a draft and one
     * the product form's readiness panel distinguishes — a control that coerced empty to `0` would
     * quietly mark an unpriced product ready to publish at no charge.
     */
    host.control.setValue(129);
    fixture.detectChanges();
    type('');

    expect(host.control.value).toBeNull();
    expect(host.control.value).not.toBe(0);
  });

  it('tells zero apart from empty', () => {
    type('0');

    expect(host.control.value).toBe(0);
  });

  it('refuses a character that cannot lead to a number, without clearing the field', () => {
    /*
     * The native control's worst habit: it drops the whole value. Here the keystroke is rejected and
     * both the box and the control keep what they had.
     */
    type('12');
    type('12a');

    expect(input().value).toBe('12');
    expect(host.control.value).toBe(12);
  });

  it('lets a number be typed one character at a time', () => {
    // `-`, `1.` and `` are all things you type on the way to a number, and none of them is one.
    type('-');
    expect(host.control.value).toBeNull();

    type('-1');
    expect(host.control.value).toBe(-1);

    type('-1.');
    expect(host.control.value).toBe(-1);

    type('-1.5');
    expect(host.control.value).toBe(-1.5);
  });

  it('accepts a comma as the decimal separator', () => {
    // Half the world types it, and the box is not going to argue about which half.
    type('12,5');

    expect(host.control.value).toBe(12.5);
  });

  /* ---------------------------------------------------------------------- blur ---- */

  it('tidies the figure when the field is left', () => {
    host.decimals.set(2);
    fixture.detectChanges();
    type('7.129');
    blur();

    expect(input().value).toBe('7.13');
    expect(host.control.value).toBe(7.13);
  });

  it('rounds to a whole number where the column is an integer', () => {
    host.decimals.set(0);
    fixture.detectChanges();
    type('4.7');
    blur();

    expect(host.control.value).toBe(5);
  });

  it('marks the control touched when the field is left', () => {
    expect(host.control.touched).toBe(false);
    blur();

    expect(host.control.touched).toBe(true);
  });

  /* -------------------------------------------------------------------- affixes ---- */

  it('shows a currency before the figure and a unit after it, without either joining the value', () => {
    host.prefix.set('SAR');
    host.suffix.set('kg');
    fixture.detectChanges();
    type('12');

    const affixes = [...element.querySelectorAll('.number-affix')].map((node) => node.textContent!.trim());
    expect(affixes).toEqual(['SAR', 'kg']);
    expect(host.control.value).toBe(12);
    // Decoration on the figure, not part of it — a reader hears the label, not "SAR twelve kg".
    expect(element.querySelector('.number-affix')?.getAttribute('aria-hidden')).toBe('true');
  });

  /* ------------------------------------------------------------------- validity ---- */

  it('lets the consumer’s own validators speak', () => {
    host.control.setValidators(Validators.required);
    // Typed into, then cleared — the field emits only on a real change, so clearing an already
    // empty box is correctly a no-op and would never run the validator.
    type('5');
    type('');

    expect(host.control.hasError('required')).toBe(true);
  });

  it('stops taking input when the form disables it', () => {
    host.control.disable();
    fixture.detectChanges();

    expect(input().disabled).toBe(true);
  });
});
