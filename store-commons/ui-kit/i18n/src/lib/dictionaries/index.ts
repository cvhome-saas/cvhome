import type {Translation} from '@jsverse/transloco';

import ar from './ar.json';
import en from './en.json';

export type KitLocale = 'en' | 'ar';

/**
 * The copy the kit's own code reads, in the two locales the console ships.
 *
 * Two namespaces, both named for what they are rather than for any one application:
 *   `shared.*`  the control catalogue's own words — pagination, dialogs, pickers, the theme names
 *   `errors.*`  the `errors.code.x → errors.category.x → errors.generic` chain `ApiErrorService` walks
 *
 * JSON rather than a TypeScript literal so the repo's i18n lint scripts can read it the same way
 * they read an application's dictionary. They have to: after the split, a key's definition and its
 * only use routinely sit on opposite sides of the package boundary.
 */
export const KIT_DICTIONARIES: Readonly<Record<KitLocale, Translation>> = {
  en: en as Translation,
  ar: ar as Translation,
};

/**
 * The kit's copy with the application's laid over the top.
 *
 * Deep rather than shallow, and app-wins: both dictionaries have a `shared` key, so a shallow merge
 * would drop one of them outright depending on argument order. App-wins so a consumer can override a
 * single string — `shared.pagination.next`, say — without forking the namespace around it.
 *
 * Called from the consumer's `TranslocoLoader`, which is the one place that knows both halves; a
 * library has no way to find an application's dictionary on its own.
 */
export function withKitCopy(appDictionary: Translation, locale: string): Translation {
  const kit = KIT_DICTIONARIES[locale as KitLocale] ?? KIT_DICTIONARIES.en;
  return deepMerge(kit, appDictionary);
}

function deepMerge(base: Translation, override: Translation): Translation {
  const out: Translation = {...base};
  for (const [key, value] of Object.entries(override)) {
    const existing = out[key];
    out[key] =
      isPlainObject(existing) && isPlainObject(value)
        ? deepMerge(existing as Translation, value as Translation)
        : value;
  }
  return out;
}

function isPlainObject(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}
