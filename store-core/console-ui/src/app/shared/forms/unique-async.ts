import {AbstractControl, AsyncValidatorFn, ValidationErrors} from '@angular/forms';
import {Observable, catchError, first, map, of, switchMap, timer} from 'rxjs';

/** How long the field is left alone before a lookup is worth making. */
const DEBOUNCE_MS = 300;

export interface UniqueAsyncOptions {
  /** Milliseconds of quiet before the check runs. */
  readonly debounceMs?: number;
  /**
   * Whether this value is worth asking the server about at all. A value that already fails a sync
   * validator should not cost a round trip, and the sync error is the one to show anyway.
   */
  readonly when?: (value: string) => boolean;
}

/**
 * "Is this already taken?", as one validator.
 *
 * Written three times before this — `catalogue-form.service.ts`'s `uniqueCode`,
 * `product-form.service.ts`'s `uniqueSku` and `create-store.facade.ts`'s `uniqueName` — with the
 * same five operators in the same order each time, because each was copied from the last. Two
 * behaviours in it are load-bearing and easy to lose in a fourth copy:
 *
 * **`timer` at the head is the debounce.** Angular cancels the previous run when the value changes
 * again, so the timer never has to be cancelled by hand; the switchMap simply never reaches the
 * call. Putting `debounceTime` after the call instead would issue every request and throw away the
 * answers.
 *
 * **`control.enabled` at the point of *reporting*, not of starting.** This is the bug the catalogue
 * shipped with. A record's code is disabled once loaded, because a code identifies the record — but
 * the form is filled while the control is still enabled, so the check starts, and the facade
 * disables the control a tick later. `disable()` nulls the errors present at that moment; it cannot
 * null one that has not arrived yet. The answer then landed on a disabled control where nothing
 * would ever clear it, marking every existing category, brand, type, group and product as a
 * duplicate of itself.
 *
 * **A check that could not be made is not a failed check.** An unreachable endpoint answers `null`,
 * leaving the field usable — the server still has the last word when the form is saved. Locking a
 * field because a lookup timed out is the worse failure.
 */
export function uniqueAsync(
  check: (value: string) => Observable<boolean>,
  errorKey = 'taken',
  options: UniqueAsyncOptions = {},
): AsyncValidatorFn {
  const debounceMs = options.debounceMs ?? DEBOUNCE_MS;

  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    const value = String(control.value ?? '').trim();
    if (!value || (options.when && !options.when(value))) {
      return of(null);
    }
    return timer(debounceMs).pipe(
      switchMap(() => check(value)),
      map((taken) => (taken && control.enabled ? {[errorKey]: true} : null)),
      // Never fail the field on a failed lookup — see the note above.
      catchError(() => of(null)),
      first(),
    );
  };
}
