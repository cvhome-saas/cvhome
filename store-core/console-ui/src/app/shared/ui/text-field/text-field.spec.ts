import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, ReactiveFormsModule} from '@angular/forms';

import {TextField, type UniquenessCheck} from './text-field';

@Component({
  imports: [TextField, ReactiveFormsModule],
  template: `
    <app-text-field
      [formControl]="control"
      [type]="type()"
      [latin]="latin()"
      [maxLength]="maxLength()"
      [check]="check()"
      [id]="fieldId()"
      ariaLabel="SKU"
      revealLabel="Show password"
      checkLabel="Checking"
      (valueChange)="normalise($event)"
    />
  `,
})
class Host {
  /** Stands in for the domain field, which strips a pasted scheme as it is typed. */
  normalise(value: string): void {
    const trimmed = value.replace(/^https?:\/\//, '');
    if (trimmed !== value) {
      this.control.setValue(trimmed);
    }
  }

  readonly control = new FormControl('', {nonNullable: true});
  readonly type = signal<'text' | 'password'>('text');
  readonly latin = signal(false);
  readonly maxLength = signal<number | null>(null);
  readonly check = signal<UniquenessCheck>('idle');
  readonly fieldId = signal<string | null>(null);
}

describe('TextField', () => {
  let fixture: ComponentFixture<Host>;
  let host: Host;
  let element: HTMLElement;

  const input = () => element.querySelector('input') as HTMLInputElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({imports: [Host]}).compileComponents();
    fixture = TestBed.createComponent(Host);
    host = fixture.componentInstance;
    fixture.detectChanges();
    element = fixture.nativeElement as HTMLElement;
  });

  /*
   * A component with an `id` input gets that id in the DOM *as well as* on the input it draws, so
   * the host and the control carried the same one: invalid HTML, and `<label for>` resolves to the
   * host, which is not a labelable element, so the association silently does not happen. Four of
   * the six controls with an `id` input had shipped with this.
   */
  it('gives its id to the control, not to the host', () => {
    host.fieldId.set('sku-field');
    fixture.detectChanges();

    const carriers = Array.from(element.querySelectorAll('#sku-field')).map((node) => node.tagName);
    expect(carriers).toEqual(['INPUT']);
  });

  it('writes the control value into the box', () => {
    host.control.setValue('ACME-1');
    fixture.detectChanges();
    expect(input().value).toBe('ACME-1');
  });

  it('publishes what is typed', () => {
    input().value = 'ACME-2';
    input().dispatchEvent(new Event('input'));
    expect(host.control.value).toBe('ACME-2');
  });

  /*
   * The order `onInput` writes in, pinned.
   *
   * Setting a `model()` emits synchronously, so a host listening to `(valueChange)` runs *inside*
   * the input handler. The domain field does exactly that — it normalises a pasted URL down to a
   * bare hostname and writes it back — and with the writes the other way round the raw value
   * landed on the control immediately afterwards, so the paste survived and the normalisation
   * appeared not to work at all.
   */
  it('lets a listener normalise what was typed without being overwritten by it', () => {
    input().value = 'https://example.com';
    input().dispatchEvent(new Event('input'));
    fixture.detectChanges();

    expect(host.control.value).toBe('example.com');
    expect(input().value).toBe('example.com');
  });

  it('reports touched on blur, so the error can show', () => {
    expect(host.control.touched).toBeFalse();
    input().dispatchEvent(new Event('blur'));
    expect(host.control.touched).toBeTrue();
  });

  it('disables the box when the control is disabled', () => {
    host.control.disable();
    fixture.detectChanges();
    expect(input().disabled).toBeTrue();
  });

  describe('password', () => {
    beforeEach(() => {
      host.type.set('password');
      fixture.detectChanges();
    });

    it('masks by default and reveals on request', () => {
      expect(input().type).toBe('password');

      const reveal = element.querySelector('.text-reveal') as HTMLButtonElement;
      expect(reveal.getAttribute('aria-pressed')).toBe('false');

      reveal.click();
      fixture.detectChanges();

      expect(input().type).toBe('text');
      expect(reveal.getAttribute('aria-pressed')).toBe('true');
    });

    it('keeps the reveal out of the tab order — the field is the stop', () => {
      const reveal = element.querySelector('.text-reveal') as HTMLButtonElement;
      expect(reveal.getAttribute('tabindex')).toBe('-1');
    });
  });

  describe('latin data in a right-to-left page', () => {
    it('pins direction so a SKU does not reorder', () => {
      host.latin.set(true);
      fixture.detectChanges();
      expect(input().getAttribute('dir')).toBe('ltr');
      expect(getComputedStyle(input()).unicodeBidi).toBe('plaintext');
    });

    it('follows the page otherwise', () => {
      expect(input().getAttribute('dir')).toBeNull();
    });
  });

  describe('the length counter', () => {
    it('is absent until a limit is set', () => {
      expect(element.querySelector('.text-counter')).toBeNull();
    });

    it('counts what is typed against the limit, and caps the box', () => {
      host.maxLength.set(10);
      host.control.setValue('abcd');
      fixture.detectChanges();

      expect(element.querySelector('.text-counter')?.textContent?.trim()).toBe('4 / 10');
      expect(input().getAttribute('maxlength')).toBe('10');
    });
  });

  describe('the uniqueness check', () => {
    it('draws nothing while idle', () => {
      expect(element.querySelector('.text-check')).toBeNull();
    });

    it('distinguishes taken from free by shape, not only by colour', () => {
      host.check.set('free');
      fixture.detectChanges();
      const free = element.querySelector('.text-check svg path')?.getAttribute('d');

      host.check.set('taken');
      fixture.detectChanges();
      const taken = element.querySelector('.text-check svg path')?.getAttribute('d');

      expect(free).toBeTruthy();
      expect(taken).toBeTruthy();
      expect(taken).not.toBe(free);
    });
  });
});
