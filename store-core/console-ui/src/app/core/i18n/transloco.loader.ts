import {Injectable} from '@angular/core';
import {TranslocoLoader, Translation} from '@jsverse/transloco';
import {from} from 'rxjs';

import type {LocaleCode} from './locale.service';

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
    return from(load().then((module) => module.default));
  }
}
