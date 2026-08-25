import {AbstractControl, ValidationErrors} from '@angular/forms';

/**
 * The default language has to be one the store actually supports.
 *
 * A group validator rather than a validator on `language`, because it is the pair that is wrong,
 * not either control on its own — and unticking a supported language has to re-run it, which a
 * validator hanging off `language` would not. seller-ui's `defaultLanguageNotInSupported` said the
 * same thing; the error is named for the field the operator should look at.
 *
 * Shared for the same reason as `phoneNumber`: both the details section and store creation ask for
 * a default language and a supported set.
 */
export function defaultLanguageIsSupported(group: AbstractControl): ValidationErrors | null {
  const language = group.get('language')?.value as string | undefined;
  const supported = group.get('supportedLanguages')?.value as readonly string[] | undefined;

  if (!language || !supported || supported.length === 0) {
    return null;
  }
  return supported.includes(language) ? null : {defaultLanguageNotSupported: true};
}
