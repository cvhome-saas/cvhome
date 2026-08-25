import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, ReactiveFormsModule, Validators} from '@angular/forms';

import {SERVER_ERROR_KEY} from '@core/errors/form-error.utils';
import {translocoTesting} from '@testing/transloco-testing';
import {FormField} from './form-field';
import {TextField} from '@shared/ui/text-field/text-field';

@Component({
  imports: [FormField, TextField, ReactiveFormsModule],
  template: `
    <app-form-field
      label="Store name"
      [control]="control"
      [required]="required()"
      [hint]="hint()"
      [fallback]="fallback()"
    >
      <app-text-field [formControl]="control" ariaLabel="Store name" />
    </app-form-field>
  `,
})
class Host {
  readonly control = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(5)],
  });
  readonly required = signal(true);
  readonly hint = signal<string | null>(null);
  readonly fallback = signal('');
}

describe('FormField', () => {
  let fixture: ComponentFixture<Host>;
  let host: Host;
  let element: HTMLElement;

  const error = () => element.querySelector('.field-error')?.textContent?.trim() ?? null;

  beforeEach(async () => {
    const transloco = translocoTesting();
    await TestBed.configureTestingModule({
      imports: [Host, ...(transloco.imports as never[])],
      providers: transloco.providers,
    }).compileComponents();
    fixture = TestBed.createComponent(Host);
    host = fixture.componentInstance;
    fixture.detectChanges();
    element = fixture.nativeElement as HTMLElement;
  });

  it('associates the projected control with the label', () => {
    // The control is a descendant of the <label>, which is the association — no for/id pair needed.
    const label = element.querySelector('label') as HTMLLabelElement;
    expect(label.querySelector('app-text-field')).not.toBeNull();
    expect(label.textContent).toContain('Store name');
  });

  it('marks required for sighted readers only — the control carries the fact', () => {
    const marker = element.querySelector('.required') as HTMLElement;
    expect(marker.textContent?.trim()).toBe('*');
    expect(marker.getAttribute('aria-hidden')).toBe('true');
  });

  it('says nothing until the operator has been near the field', () => {
    expect(host.control.invalid).toBeTrue();
    expect(error()).toBeNull();
  });

  describe('once touched', () => {
    beforeEach(() => {
      host.control.markAsTouched();
      fixture.detectChanges();
    });

    it('names the failure from the shared vocabulary rather than a per-field key', () => {
      expect(error()).toBe('This field is required.');
    });

    it('moves to the next failure rather than repeating the first', () => {
      // The bug the shared map exists to fix: a name over its limit used to report "is required".
      host.control.setValue('far too long');
      fixture.detectChanges();
      expect(error()).toBe('Use at most 5 characters.');
    });

    it('prefers the server’s own message over anything local', () => {
      host.fallback.set('Pick something friendlier.');
      host.control.setErrors({
        [SERVER_ERROR_KEY]: {field: 'name', code: 'X', message: 'That name is reserved.'},
      });
      fixture.detectChanges();
      expect(error()).toBe('That name is reserved.');
    });

    it('prefers a sentence the field wrote over the shared vocabulary', () => {
      // The map removes boilerplate; it must not overrule a call site that knows better. Two
      // store-management fields — the custom domain and the support phone — caught this.
      host.fallback.set('Enter a valid host name.');
      fixture.detectChanges();
      expect(error()).toBe('Enter a valid host name.');
    });

    it('says nothing for a validator neither the map nor the field describes', () => {
      host.control.setValue('ok');
      host.control.setErrors({somethingBespoke: true});
      fixture.detectChanges();
      expect(error()).toBeNull();
    });
  });

  it('renders a hint under the control when there is one', () => {
    expect(element.querySelector('.field-hint')).toBeNull();
    host.hint.set('Shown to shoppers at checkout.');
    fixture.detectChanges();
    expect(element.querySelector('.field-hint')?.textContent?.trim())
      .toBe('Shown to shoppers at checkout.');
  });
});
