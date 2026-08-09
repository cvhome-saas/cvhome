import {TestBed} from '@angular/core/testing';
import {FormControl, FormGroup} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {NOTIFICATION_PORT} from './notification.port';
import {ApiError} from './api-error';
import {ApiErrorService, categoryToKey, codeToKey} from './api-error.service';
import {serverErrorOf} from './form-error.utils';

/** ngx-translate returns the key itself when it cannot resolve one — this fake keeps that contract. */
class FakeTranslateService {
  constructor(private readonly messages: Record<string, string>) {}

  instant(key: string, params?: Record<string, unknown>): string {
    const template = this.messages[key];
    if (template === undefined) {
      return key;
    }
    return template.replace(/\{\{\s*(\w+)\s*}}/g, (_, name: string) => String(params?.[name] ?? `{{${name}}}`));
  }
}

class SpyNotificationService {
  readonly dangers: string[] = [];

  danger(message: string): void {
    this.dangers.push(message);
  }
}

const MESSAGES: Record<string, string> = {
  'ERRORS.CODE.CATALOG_PRODUCT_VARIANT_SKU_CONFLICT': 'SKU {{variantSku}} is already used by another variant.',
  'ERRORS.CATEGORY.CONFLICT': 'That conflicts with something that already exists.',
  'ERRORS.CATEGORY.INTERNAL': 'Something went wrong on our side. Please try again.',
  'ERRORS.CATEGORY.NOT_FOUND': 'We couldn\'t find what you asked for.',
  'ERRORS.GENERIC': 'Something went wrong. Please try again.',
  'ERRORS.TRACE': 'Reference: {{traceId}}',
  'ERRORS.CODE.NOT_BLANK': 'This field is required.',
};

function setup(messages: Record<string, string> = MESSAGES) {
  const notifications = new SpyNotificationService();
  TestBed.configureTestingModule({
    providers: [
      ApiErrorService,
      {provide: TranslateService, useValue: new FakeTranslateService(messages)},
      {provide: NOTIFICATION_PORT, useValue: notifications},
    ],
  });
  return {service: TestBed.inject(ApiErrorService), notifications};
}

function apiError(init: Partial<ApiError> & {code: string; category: ApiError['category']; status: number}): ApiError {
  return new ApiError({
    code: init.code, category: init.category, status: init.status,
    traceId: init.traceId, params: init.params, fieldErrors: init.fieldErrors,
  });
}

describe('codeToKey', () => {

  it('flattens dots, because ngx-translate reads them as nesting', () => {
    expect(codeToKey('CATALOG.PRODUCT.NOT_FOUND')).toBe('ERRORS.CODE.CATALOG_PRODUCT_NOT_FOUND');
    expect(categoryToKey('NOT_FOUND')).toBe('ERRORS.CATEGORY.NOT_FOUND');
  });

});

describe('ApiErrorService.messageFor', () => {

  it('prefers the code message and interpolates its params', () => {
    const {service} = setup();

    const message = service.messageFor(apiError({
      code: 'CATALOG.PRODUCT_VARIANT.SKU_CONFLICT', category: 'CONFLICT', status: 409,
      params: {variantSku: 'ABC-1'},
    }));

    expect(message).toBe('SKU ABC-1 is already used by another variant.');
  });

  it('falls back to the category when the code has no translation', () => {
    const {service} = setup();

    // A LEGACY.* code, or any of the 126 that are not curated — the category is what keeps it readable.
    const message = service.messageFor(apiError({code: 'LEGACY.SERVICE_ERROR', category: 'INTERNAL', status: 500}));

    expect(message).toBe('Something went wrong on our side. Please try again.');
  });

  it('falls back to GENERIC when neither resolves, so the chain always terminates', () => {
    const {service} = setup({'ERRORS.GENERIC': 'Something went wrong. Please try again.'});

    const message = service.messageFor(apiError({code: 'X.Y', category: 'UNKNOWN', status: 0}));

    expect(message).toBe('Something went wrong. Please try again.');
  });

  it('never returns a raw translation key', () => {
    const {service} = setup({'ERRORS.GENERIC': 'Something went wrong. Please try again.'});

    const message = service.messageFor(apiError({code: 'NOT.TRANSLATED', category: 'CONFLICT', status: 409}));

    expect(message).not.toContain('ERRORS.');
  });

});

describe('ApiErrorService.notify', () => {

  it('appends the trace reference for a server-side failure', () => {
    const {service, notifications} = setup();

    service.notify(apiError({code: 'COMMON.INTERNAL_ERROR', category: 'INTERNAL', status: 500, traceId: '3f9a1c8e'}));

    expect(notifications.dangers[0]).toContain('Reference: 3f9a1c8e');
  });

  it('does not append it for a failure the seller caused', () => {
    // "That SKU is already taken" needs no support reference; printing one everywhere trains people to
    // ignore it.
    const {service, notifications} = setup();

    service.notify(apiError({
      code: 'CATALOG.PRODUCT_VARIANT.SKU_CONFLICT', category: 'CONFLICT', status: 409,
      params: {variantSku: 'ABC-1'}, traceId: '3f9a1c8e',
    }));

    expect(notifications.dangers[0]).not.toContain('3f9a1c8e');
  });

  it('never puts developer detail in front of the seller', () => {
    const {service, notifications} = setup();
    const error = new ApiError({
      code: 'COMMON.INTERNAL_ERROR', category: 'INTERNAL', status: 500,
      debugDetail: 'NullPointerException at ProductServiceImpl.java:214',
    });

    service.notify(error);

    expect(notifications.dangers[0]).not.toContain('NullPointerException');
  });

});

describe('ApiErrorService with something that is not an ApiError', () => {

  // Regression: the interceptor only sees what passes through it. An error thrown downstream — inside a
  // `map`, as AuthService does — never does, so `catchError((e: ApiError) => ...)` is a cast and not a
  // guarantee. This used to crash on `codeToKey(undefined)` and swallow the real failure.
  it('does not crash on a raw TypeError, and still says something', () => {
    const {service, notifications} = setup();

    expect(() => service.notify(new TypeError("Cannot read properties of undefined (reading 'principal')")))
      .not.toThrow();
    expect(notifications.dangers[0]).toBe('Something went wrong. Please try again.');
  });

  it('does not crash on undefined or a string', () => {
    const {service} = setup();

    expect(() => service.notify(undefined)).not.toThrow();
    expect(() => service.notify('boom')).not.toThrow();
    expect(service.messageFor(undefined)).not.toContain('ERRORS.');
  });

  it('applyToForm survives one too', () => {
    const {service, notifications} = setup();
    const form = new FormGroup({sku: new FormControl('')});

    expect(() => service.applyToForm(new TypeError('nope'), form)).not.toThrow();
    expect(notifications.dangers.length).toBe(1);
  });

});

describe('ApiErrorService.applyToForm', () => {

  it('binds field errors to their controls', () => {
    const {service, notifications} = setup();
    const form = new FormGroup({sku: new FormControl('')});

    service.applyToForm(apiError({
      code: 'COMMON.VALIDATION_FAILED', category: 'VALIDATION', status: 400,
      fieldErrors: [{field: 'sku', code: 'NOT.BLANK', message: 'must not be blank'}],
    }), form);

    expect(serverErrorOf(form.get('sku'))?.code).toBe('NOT.BLANK');
    expect(notifications.dangers).toEqual([]);
  });

  it('toasts what had no control to attach to, rather than dropping it', () => {
    const {service, notifications} = setup();
    const form = new FormGroup({sku: new FormControl('')});

    service.applyToForm(apiError({
      code: 'COMMON.VALIDATION_FAILED', category: 'VALIDATION', status: 400,
      fieldErrors: [{field: 'objectLevel', code: 'NOT.BLANK', message: 'must not be blank'}],
    }), form);

    expect(notifications.dangers.length).toBe(1);
  });

  it('toasts when a validation error carries no field errors at all', () => {
    // Otherwise the submit button appears to do nothing.
    const {service, notifications} = setup();
    const form = new FormGroup({sku: new FormControl('')});

    service.applyToForm(apiError({code: 'COMMON.VALIDATION_FAILED', category: 'VALIDATION', status: 400}), form);

    expect(notifications.dangers.length).toBe(1);
  });

});
