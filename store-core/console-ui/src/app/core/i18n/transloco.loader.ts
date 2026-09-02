import {Injectable} from '@angular/core';
import {TranslocoLoader, Translation} from '@jsverse/transloco';
import {withKitCopy} from '@cvhome-saas/ui-kit/i18n';
import {from} from 'rxjs';

import type {LocaleCode} from '@cvhome-saas/ui-kit/i18n';

/**
 * Static imports rather than Transloco's default `HttpLoader`: the `HttpLoader` fetches a
 * relative `/assets/i18n/{lang}.json` URL, which has no origin to resolve against during
 * SSR. A static import map is bundled and code-split by esbuild instead, and behaves
 * identically on the server and in the browser.
 */
const DICTIONARIES: Record<LocaleCode, () => Promise<{default: Translation}>> = {
  en: () => import('@i18n/en.json'),
  ar: () => import('@i18n/ar.json'),
};

@Injectable({providedIn: 'root'})
export class TranslocoDictionaryLoader implements TranslocoLoader {
  getTranslation(lang: string) {
    const load = DICTIONARIES[lang as LocaleCode] ?? DICTIONARIES.en;
    // The kit's own copy underneath, this app's on top. `shared.*` and `errors.*` are read by code
    // that now lives in the library, so the strings ship with it; anything here of the same name
    // wins, which is how the console can override one message without forking a namespace.
    return from(load().then((module) => withKitCopy(module.default, lang)));
  }
}
