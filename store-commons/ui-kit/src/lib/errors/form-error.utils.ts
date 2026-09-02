import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {AbstractControl} from '@angular/forms';
import {ProblemFieldError} from './problem-detail.model';

/** The single key every server-side field failure is stored under. */
export const SERVER_ERROR_KEY = 'server';

export interface FieldErrorOptions {
  /** Mark each matched control as touched, so templates gating on `dirty || touched` actually show it. */
  readonly markAsTouched?: boolean;
}

/**
 * Binds `fieldErrors` to the controls that caused them and returns whatever could not be matched.
 *
 * Two invariants make this survive real use:
 *
 * 1. **The error key is always `server`**, and its value is the whole `ProblemFieldError`. One branch for
 *    the display component, and `code` + `params` ride along for translation.
 * 2. **The caller must handle the unmatched.** An object-level `@AssertTrue` has no control to attach to,
 *    and silently dropping it means the seller sees a form that refuses to submit with nothing marked.
 */
export function applyFieldErrors(form: AbstractControl,
                                 fieldErrors: readonly ProblemFieldError[],
                                 options?: FieldErrorOptions): ProblemFieldError[] {
  const unmatched: ProblemFieldError[] = [];

  for (const fieldError of fieldErrors) {
    const control = resolveControl(form, fieldError.field);
    if (!control) {
      unmatched.push(fieldError);
      continue;
    }
    control.setErrors({...(control.errors ?? {}), [SERVER_ERROR_KEY]: fieldError});
    if (options?.markAsTouched !== false) {
      control.markAsTouched();
    }
  }

  return unmatched;
}

/**
 * Finds the control a server path names.
 *
 * `GlobalErrorHandler` sends bean paths (`endpoint.endpoint`), while `ConstraintViolationErrorHandler`
 * prefixes the method name (`createPod.pod.name`). No rule can tell those apart by looking at the string —
 * `productType.code` is exactly as method-shaped as `createPod.pod`. So we ask the form: try the full path,
 * and only when that misses, retry without the leading segment. The form is the one oracle that knows.
 */
function resolveControl(form: AbstractControl, field: string): AbstractControl | null {
  const direct = form.get(field);
  if (direct) {
    return direct;
  }
  const segments = field.split('.');
  return segments.length > 1 ? form.get(segments.slice(1).join('.')) : null;
}

/**
 * Clears server errors as soon as the user edits the form.
 *
 * Without this the form stays permanently invalid: a server error is not a validator, so nothing else will
 * ever remove it, and the seller fixes the field only to find submit still disabled.
 *
 * Deletes the whole `errors` object when `server` was the only key, because Angular treats an empty object
 * as "invalid with no errors" rather than valid.
 */
export function clearServerErrorsOnChange(form: AbstractControl, destroyRef: DestroyRef): void {
  form.valueChanges
    .pipe(takeUntilDestroyed(destroyRef))
    .subscribe(() => clearServerErrors(form));
}

/** Strips every `server` error in the tree. Exported for the submit path, which clears before re-posting. */
export function clearServerErrors(control: AbstractControl): void {
  const children = childControls(control);
  if (children.length > 0) {
    children.forEach(clearServerErrors);
    return;
  }

  const errors = control.errors;
  if (!errors || !(SERVER_ERROR_KEY in errors)) {
    return;
  }

  const rest = Object.fromEntries(Object.entries(errors).filter(([key]) => key !== SERVER_ERROR_KEY));

  // `setErrors` already recomputes this control's status and propagates it to its ancestors. Following it
  // with `updateValueAndValidity` would re-run the validators and overwrite the very object we just built,
  // discarding the client-side errors we are trying to preserve.
  control.setErrors(Object.keys(rest).length > 0 ? rest : null, {emitEvent: false});
}

function childControls(control: AbstractControl): AbstractControl[] {
  const container = control as unknown as {controls?: Record<string, AbstractControl> | AbstractControl[]};
  if (!container.controls) {
    return [];
  }
  return Array.isArray(container.controls) ? container.controls : Object.values(container.controls);
}

/** Reads a server error off a control, for the display component. */
export function serverErrorOf(control: AbstractControl | null | undefined): ProblemFieldError | null {
  const value = control?.errors?.[SERVER_ERROR_KEY];
  return value ? value as ProblemFieldError : null;
}
