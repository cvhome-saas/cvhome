import {Injectable} from '@angular/core';
import type {Translation, TranslocoLoader} from '@jsverse/transloco';
import {withKitCopy} from '@cvhome-saas/ui-kit/i18n';
import {from} from 'rxjs';

export type LocaleCode = 'en' | 'ar';

/**
 * Static imports rather than Transloco's `HttpLoader`, for the same reason console-ui uses them: a
 * relative `/assets/i18n/{lang}.json` has no origin to resolve against, and a bundled import map is
 * code-split by esbuild and behaves identically wherever it runs.
 */
const DICTIONARIES: Record<LocaleCode, () => Promise<{default: Translation}>> = {
  en: () => import('@i18n/en.json'),
  ar: () => import('@i18n/ar.json'),
};

@Injectable({providedIn: 'root'})
export class TranslocoDictionaryLoader implements TranslocoLoader {
  getTranslation(lang: string) {
    const load = DICTIONARIES[lang as LocaleCode] ?? DICTIONARIES.en;
    // The kit's copy underneath, this app's on top — `shared.*` and `errors.*` are read by code that
    // lives in the library, so the strings ship with it.
    return from(load().then((module) => withKitCopy(module.default, lang)));
  }
}
