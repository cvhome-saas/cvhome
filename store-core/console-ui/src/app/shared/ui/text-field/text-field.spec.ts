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
      ariaLabel="SKU"
      revealLabel="Show password"
      checkLabel="Checking"
    />
  `,
})
class Host {
  readonly control = new FormControl('', {nonNullable: true});
  readonly type = signal<'text' | 'password'>('text');
  readonly latin = signal(false);
  readonly maxLength = signal<number | null>(null);
  readonly check = signal<UniquenessCheck>('idle');
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
