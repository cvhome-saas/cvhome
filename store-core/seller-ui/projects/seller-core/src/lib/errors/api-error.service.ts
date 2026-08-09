import {inject, Injectable} from '@angular/core';
import {AbstractControl} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {NOTIFICATION_PORT} from './notification.port';
import {ApiError} from './api-error';
import {toApiError} from './problem-detail.parser';
import {applyFieldErrors, FieldErrorOptions} from './form-error.utils';
import {ProblemFieldError} from './problem-detail.model';

/**
 * ngx-translate treats a dot as nesting, and every backend code is dotted
 * (`CATALOG.PRODUCT.NOT_FOUND`), so keys are flattened. One exported helper, used by the service and by
 * any tooling that needs to check coverage.
 */
export function codeToKey(code: string): string {
  return `ERRORS.CODE.${code.replace(/\./g, '_')}`;
}

export function categoryToKey(category: string): string {
  return `ERRORS.CATEGORY.${category}`;
}

/**
 * Owns the one path from an `ApiError` to something a seller reads.
 *
 * The chain is `ERRORS.CODE.<code>` → `ERRORS.CATEGORY.<category>` → `ERRORS.GENERIC`. The category rung is
 * load-bearing rather than a nicety: 126 codes exist and only the ones a seller actually meets are
 * translated, plus ~125 `LEGACY.*` throw sites remain un-migrated on the backend. Those all arrive with a
 * correct category, so the fallback is what keeps them readable until the backend finishes.
 */
@Injectable({providedIn: 'root'})
export class ApiErrorService {

  private readonly translate = inject(TranslateService);

  private readonly notifications = inject(NOTIFICATION_PORT);

  /** The message a seller should see. Never `detail`, which is developer text. */
  messageFor(raw: unknown): string {
    const error = this.normalize(raw);
    return this.resolve(codeToKey(error.code), error.params)
      ?? this.resolve(categoryToKey(error.category))
      ?? this.translate.instant('ERRORS.GENERIC');
  }

  /**
   * Shows the error as a toast.
   *
   * The trace reference is appended only when the failure is ours — a seller does not need a support
   * reference for "that SKU is already taken", and printing one on every validation error trains them to
   * ignore it.
   */
  notify(raw: unknown): void {
    const error = this.normalize(raw);
    let message = this.messageFor(error);
    if (error.isServerSide && error.traceId) {
      message += `\n${this.translate.instant('ERRORS.TRACE', {traceId: error.traceId})}`;
    }
    console.error('API error', error.toLogContext());
    this.notifications.danger(message);
  }

  /**
   * Binds `fieldErrors` to the form and toasts whatever had no control to attach to.
   *
   * A validation failure with no `fieldErrors` at all still gets a toast — otherwise the submit button
   * appears to do nothing.
   */
  applyToForm(raw: unknown, form: AbstractControl, options?: FieldErrorOptions): void {
    const error = this.normalize(raw);
    if (error.fieldErrors.length === 0) {
      this.notify(error);
      return;
    }

    const unmatched = applyFieldErrors(form, error.fieldErrors, options);
    console.error('API error', error.toLogContext());

    if (unmatched.length > 0) {
      this.notifications.danger(unmatched.map(it => this.fieldMessage(it)).join('\n'));
    }
  }

  /**
   * A message for one field error, for the display component.
   *
   * Prefers our own translation of the field's code and falls back to the server's `message`, which
   * bean-validation fills with the constraint's default text — English, but better than nothing.
   */
  fieldMessage(fieldError: ProblemFieldError): string {
    return this.resolve(codeToKey(fieldError.code), fieldError.params)
      ?? fieldError.message
      ?? this.translate.instant('ERRORS.GENERIC');
  }

  /**
   * These methods take `unknown` on purpose.
   *
   * The interceptor guarantees that everything *it* sees becomes an `ApiError` — but an error thrown
   * downstream of it, inside a `map` or a `tap`, never passes through it at all. A `catchError((e: ApiError)
   * => ...)` annotation is a cast, not a guarantee, and one such error reaching `messageFor` used to crash
   * on `codeToKey(undefined)`. `toApiError` is idempotent, so normalising here costs nothing and makes the
   * funnel honest.
   */
  private normalize(raw: unknown): ApiError {
    return toApiError(raw);
  }

  /**
   * `instant` returns the key itself when it cannot resolve one, which is how a missing translation is
   * detected — there is no "has this key" API that accounts for the fallback language.
   */
  private resolve(key: string, params?: Record<string, unknown>): string | null {
    const value: unknown = this.translate.instant(key, params);
    if (typeof value !== 'string' || value === key || value.length === 0) {
      return null;
    }
    return value;
  }
}
