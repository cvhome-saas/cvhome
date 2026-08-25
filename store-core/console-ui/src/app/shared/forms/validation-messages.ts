import {ValidationErrors} from '@angular/forms';

/**
 * What a failed validator is called, in words.
 *
 * Every field in the console used to say what was wrong with it by hand: `<app-field-error
 * [fallback]="t('storeSettings.details.nameRequired')" />`, thirty times over, with a key authored
 * per field. That has two costs. The obvious one is thirty keys in two locales saying "Required".
 * The other is that a control carrying *two* possible failures — required and too long, which is
 * most text fields — could only ever show one sentence, so a name over its limit reported
 * "Store name is required" while the operator looked at the name they had typed.
 *
 * A validator's key is its own name, and the parameters Angular already puts in the error object
 * are passed straight through, so `maxlength` can say how long is too long without the call site
 * knowing. `shared.validation.*` holds the sentences.
 *
 * A field with something genuinely specific to say still says it: `app-field-error`'s `fallback`
 * takes precedence over this map, and is now the exception rather than the only mechanism. The map
 * removes boilerplate; it does not overrule a sentence someone wrote on purpose.
 */
const MESSAGE_KEYS: Readonly<Record<string, string>> = {
  required: 'shared.validation.required',
  email: 'shared.validation.email',
  minlength: 'shared.validation.minLength',
  maxlength: 'shared.validation.maxLength',
  min: 'shared.validation.min',
  max: 'shared.validation.max',
  pattern: 'shared.validation.pattern',
  url: 'shared.validation.url',
  phone: 'shared.validation.phone',
  taken: 'shared.validation.taken',
  mismatch: 'shared.validation.mismatch',
};

/**
 * The order failures are reported in.
 *
 * A control that is both empty and malformed is empty first — telling someone their blank field is
 * not a valid email address is technically true and useless. Uniqueness comes last because it is
 * the only one that costs a round trip, and it is worth saying nothing until the value is at least
 * shaped correctly.
 */
const PRECEDENCE: readonly string[] = [
  'required',
  'minlength',
  'maxlength',
  'min',
  'max',
  'email',
  'url',
  'phone',
  'pattern',
  'mismatch',
  'taken',
];

export interface ValidationMessage {
  readonly key: string;
  readonly params: Record<string, unknown>;
}

/**
 * The one message a control should show, or `null` when this map has nothing to say about it.
 *
 * Returning `null` rather than a generic "invalid" is deliberate: a validator this map does not
 * know is a validator whose call site wrote its own sentence, and overwriting that with something
 * vaguer would be a regression. The caller falls back to its `fallback`.
 */
export function validationMessage(errors: ValidationErrors | null | undefined): ValidationMessage | null {
  if (!errors) {
    return null;
  }
  for (const name of PRECEDENCE) {
    if (!(name in errors)) {
      continue;
    }
    const key = MESSAGE_KEYS[name];
    if (!key) {
      continue;
    }
    const detail = errors[name];
    return {key, params: typeof detail === 'object' && detail !== null ? {...detail} : {}};
  }
  return null;
}
