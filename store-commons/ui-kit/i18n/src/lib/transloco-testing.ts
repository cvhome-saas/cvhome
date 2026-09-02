import type {EnvironmentProviders, Provider} from '@angular/core';
import {TranslocoTestingModule} from '@jsverse/transloco';
import {provideTranslocoLocale} from '@jsverse/transloco-locale';
import {provideTranslocoMessageformat} from '@jsverse/transloco-messageformat';

import {KIT_DICTIONARIES} from './dictionaries';

/**
 * Transloco, wired for a spec, with the kit's real English copy loaded.
 *
 * The kit's counterpart to console-ui's `kitTranslocoTesting()`, and it exists for the same reason:
 * nothing on the `TranslocoService` chain defaults on its own, so a spec touching a control that
 * renders copy needs a transpiler, a config and a locale mapping or it throws before the first
 * assertion. The real dictionary rather than a stub, so `expect(…).toContain('No matches')` is
 * asserting on what an operator sees instead of on a key.
 *
 * `TranslocoTestingModule` seeds the dictionary synchronously — it writes straight into the service
 * instead of going through a loader's Observable — so translations are present before the first
 * `detectChanges()`. Nothing here calls `transloco.load()` the way an application's initializer does.
 *
 * `imports` and `providers` come back separately because the module and the provider arrays are
 * different kinds of thing; mixing them into either one is a type error.
 */
export function kitTranslocoTesting(): {
  imports: unknown[];
  providers: (Provider | EnvironmentProviders)[];
} {
  return {
    imports: [
      TranslocoTestingModule.forRoot({
        langs: {en: KIT_DICTIONARIES.en},
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
