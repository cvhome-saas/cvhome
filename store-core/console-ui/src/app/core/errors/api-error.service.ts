import {inject, Injectable} from '@angular/core';
import {AbstractControl} from '@angular/forms';
import {TranslocoService} from '@jsverse/transloco';
import {NOTIFICATION_PORT} from './notification.port';
import {ApiError} from './api-error';
import {toApiError} from './problem-detail.parser';
import {applyFieldErrors, FieldErrorOptions} from './form-error.utils';
import {ProblemFieldError} from './problem-detail.model';

/**
 * Backend codes are dotted, so keys are flattened. Exported for tests and future coverage tooling.
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

  private readonly notifications = inject(NOTIFICATION_PORT);
  private readonly transloco = inject(TranslocoService);

  /** The message a seller should see. Never `detail`, which is developer text. */
  messageFor(raw: unknown): string {
    const error = this.normalize(raw);
    return this.resolve(codeToKey(error.code), error.params)
      ?? this.resolve(categoryToKey(error.category))
      ?? this.transloco.translate('errors.generic');
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
      message += `\n${this.transloco.translate('errors.trace')} ${error.traceId}`;
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
      ?? this.transloco.translate('errors.generic');
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
   * The category rung is what keeps legacy backend codes readable: 126 codes exist and
   * only the ones a seller actually meets have a `ERRORS.CODE.*` entry below, but every
   * one of them still arrives with a correct category.
   */
  private resolve(key: string, params?: Record<string, unknown>): string | null {
    switch (key) {
      case 'ERRORS.CATEGORY.VALIDATION':
      case 'ERRORS.CATEGORY.MALFORMED':
      case 'ERRORS.CATEGORY.CONVERSION':
      case 'ERRORS.CATEGORY.UNPROCESSABLE':
        return this.transloco.translate('errors.category.validation');
      case 'ERRORS.CATEGORY.UNAUTHENTICATED':
        return this.transloco.translate('errors.category.unauthenticated');
      case 'ERRORS.CATEGORY.FORBIDDEN':
        return this.transloco.translate('errors.category.forbidden');
      case 'ERRORS.CATEGORY.NOT_FOUND':
        return this.transloco.translate('errors.category.notFound');
      case 'ERRORS.CATEGORY.CONFLICT':
        return this.transloco.translate('errors.category.conflict');
      case 'ERRORS.CATEGORY.REMOTE_SERVICE':
      case 'ERRORS.CATEGORY.TIMEOUT':
        return this.transloco.translate('errors.category.remote');
      case 'ERRORS.CATEGORY.STORAGE':
      case 'ERRORS.CATEGORY.INTERNAL':
        return this.transloco.translate('errors.category.internal');
      case 'ERRORS.CODE.CLIENT.SESSION_MISSING':
        return this.transloco.translate('errors.code.clientSessionMissing');
      default:
        return this.interpolate(params?.['message']);
    }
  }

  private interpolate(value: unknown): string | null {
    return typeof value === 'string' && value.length > 0 ? value : null;
  }
}
