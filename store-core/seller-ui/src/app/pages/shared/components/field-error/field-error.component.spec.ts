import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {AbstractControl, FormControl, FormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ApiErrorService} from '../../../../core/errors/api-error.service';
import {NotificationService} from '../../../../core/notifications/notification.service';
import {applyFieldErrors} from '../../../../core/errors/form-error.utils';
import {FieldErrorComponent} from './field-error.component';

class FakeTranslateService {
  instant(key: string, params?: Record<string, unknown>): string {
    const messages: Record<string, string> = {
      'ERRORS.FIELD.REQUIRED': 'This field is required.',
      'ERRORS.FIELD.MINLENGTH': 'Use at least {{requiredLength}} characters.',
      'POD_FORM.NAME_REQUIRED': 'A pod needs a name.',
      'ERRORS.CODE.CATALOG_PRODUCT_VARIANT_SKU_CONFLICT': 'SKU is already used by another variant.',
      'ERRORS.GENERIC': 'Something went wrong. Please try again.',
    };
    const template = messages[key];
    if (template === undefined) {
      return key;
    }
    return template.replace(/\{\{\s*(\w+)\s*}}/g, (_, name: string) => String(params?.[name] ?? ''));
  }
}

@Component({
  standalone: true,
  imports: [FieldErrorComponent],
  template: `<ngx-field-error [control]="control()" [messages]="messages()"/>`,
})
class HostComponent {
  readonly control = signal<AbstractControl>(new FormControl(''));
  readonly messages = signal<Record<string, string>>({});
}

describe('FieldErrorComponent', () => {

  let fixture: ComponentFixture<HostComponent>;
  let host: HostComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HostComponent],
      providers: [
        ApiErrorService,
        {provide: TranslateService, useValue: new FakeTranslateService()},
        // ApiErrorService pulls in NotificationService, which pulls in Nebular's toastr; this component
        // only ever calls fieldMessage, so a stub keeps the test to what it is actually about.
        {provide: NotificationService, useValue: {danger: () => undefined}},
      ],
    });
    fixture = TestBed.createComponent(HostComponent);
    host = fixture.componentInstance;
  });

  function text(): string {
    fixture.detectChanges();
    return (fixture.nativeElement as HTMLElement).textContent?.trim() ?? '';
  }

  it('shows nothing for a pristine, untouched control', () => {
    host.control.set(new FormControl('', Validators.required));

    expect(text()).toBe('');
  });

  it('shows a generic message once the control is touched', () => {
    const control = new FormControl('', Validators.required);
    host.control.set(control);
    control.markAsTouched();

    expect(text()).toBe('This field is required.');
  });

  it('prefers the per-field override when one is given', () => {
    const control = new FormControl('', Validators.required);
    host.control.set(control);
    host.messages.set({required: 'POD_FORM.NAME_REQUIRED'});
    control.markAsTouched();

    expect(text()).toBe('A pod needs a name.');
  });

  it('interpolates the validator payload', () => {
    const control = new FormControl('ab', Validators.minLength(5));
    host.control.set(control);
    control.markAsTouched();

    expect(text()).toBe('Use at least 5 characters.');
  });

  it('reacts to a touch alone, with no value change', () => {
    // The reason this bridges `control.events` rather than `valueChanges`: a control the user blurred
    // without typing emits nothing else, and every template gates on `dirty || touched`.
    const control = new FormControl('', Validators.required);
    host.control.set(control);
    expect(text()).toBe('');

    control.markAsTouched();

    expect(text()).toBe('This field is required.');
  });

  it('shows a server error immediately, without waiting for a touch', () => {
    const control = new FormControl('ABC-1');
    const form = new FormGroup({sku: control});
    host.control.set(control);
    applyFieldErrors(form, [{field: 'sku', code: 'CATALOG.PRODUCT_VARIANT.SKU_CONFLICT'}], {markAsTouched: false});

    expect(text()).toBe('SKU is already used by another variant.');
  });

  it('lets the server error win over a client validator', () => {
    // The server saw the whole request; the validator only saw this field.
    const control = new FormControl('', Validators.required);
    const form = new FormGroup({sku: control});
    host.control.set(control);
    control.markAsTouched();
    applyFieldErrors(form, [{field: 'sku', code: 'CATALOG.PRODUCT_VARIANT.SKU_CONFLICT'}]);

    expect(text()).toBe('SKU is already used by another variant.');
    expect(text()).not.toContain('required');
  });

  it('falls back to the server message when the code has no translation', () => {
    const control = new FormControl('');
    const form = new FormGroup({sku: control});
    host.control.set(control);
    applyFieldErrors(form, [{field: 'sku', code: 'NOT.TRANSLATED', message: 'must not be blank'}]);

    expect(text()).toBe('must not be blank');
  });

  it('clears when the control becomes valid', () => {
    const control = new FormControl('', Validators.required);
    host.control.set(control);
    control.markAsTouched();
    expect(text()).toBe('This field is required.');

    control.setValue('something');

    expect(text()).toBe('');
  });

});
