import {AbstractControl, ValidationErrors} from '@angular/forms';

import {PHONE_MIN_DIGITS, PHONE_PATTERN} from '@models/store-settings';

/**
 * A phone number that could be dialled.
 *
 * `Validators.pattern` alone cannot express "at least six digits, however they are grouped", and
 * that is the check worth having — `(0) - .` matches any shape rule and is not a number. Runs the
 * shape test first so the two errors stay distinguishable, and passes an empty value through:
 * whether the field is mandatory is `Validators.required`'s business, not this one's.
 *
 * Shared because a store's support number is asked for twice — on `/store-management/details` and
 * again on `/store-management/create` — and create-store was reaching across into store-management
 * to borrow this, which is the import the tier rule now refuses.
 */
export function phoneNumber(control: AbstractControl): ValidationErrors | null {
  const value = String(control.value ?? '').trim();
  if (!value) {
    return null;
  }
  if (!PHONE_PATTERN.test(value)) {
    return {phone: true};
  }
  const digits = value.replace(/\D/g, '').length;
  return digits < PHONE_MIN_DIGITS ? {phone: true} : null;
}
