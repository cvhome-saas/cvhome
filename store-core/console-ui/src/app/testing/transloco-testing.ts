import {EnvironmentProviders, Provider} from '@angular/core';
import {TranslocoTestingModule} from '@jsverse/transloco';
import {provideTranslocoLocale} from '@jsverse/transloco-locale';
import {provideTranslocoMessageformat} from '@jsverse/transloco-messageformat';

import {withKitCopy} from '@cvhome-saas/ui-kit/i18n';

import enApp from '@i18n/en.json';

const en = withKitCopy(enApp, 'en');

/**
 * Every spec that touches something on the `TranslocoService` chain — directly, or through
 * `LocaleService`/`ConsoleShellFacade`/`TranslocoLocaleService` — needs a transpiler,
 * config and locale mapping, none of which default on their own. This loads the real
 * English dictionary so assertions on rendered copy stay meaningful, rather than asserting
 * on raw keys.
 *
 * `TranslocoTestingModule` (not a hand-rolled `provideTransloco`) is what seeds the
 * dictionary synchronously: it writes straight into the service rather than going through
 * a loader's `Observable`, so translations are already there before the first
 * `fixture.detectChanges()` — nothing here ever calls `transloco.load()` the way the real
 * app's `provideAppInitializer` does.
 *
 * `imports` and `providers` are separate return values because `TranslocoTestingModule` is
 * a module and `provideTranslocoLocale` is a plain provider array — mixing them in either
 * one is a type error.
 */
export function translocoTesting(): {
  imports: unknown[];
  providers: (Provider | EnvironmentProviders)[];
} {
  return {
    imports: [
      TranslocoTestingModule.forRoot({
        langs: {en},
        translocoConfig: {availableLangs: ['en', 'ar'], defaultLang: 'en', prodMode: true},
        preloadLangs: true,
      }),
    ],
    providers: [
      provideTranslocoLocale({langToLocaleMapping: {en: 'en-US', ar: 'ar-EG'}}),
      provideTranslocoMessageformat(),
    ],
  };
}
