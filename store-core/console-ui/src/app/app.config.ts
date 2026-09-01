import {
  ApplicationConfig,
  ErrorHandler,
  inject,
  isDevMode,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
} from '@angular/core';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {provideClientHydration, withEventReplay} from '@angular/platform-browser';
import {provideRouter, TitleStrategy, withComponentInputBinding} from '@angular/router';
import {provideTransloco, provideTranslocoMissingHandler, TranslocoService} from '@jsverse/transloco';
import {provideTranslocoLocale} from '@jsverse/transloco-locale';
import {provideTranslocoMessageformat} from '@jsverse/transloco-messageformat';
import {provideTranslocoPersistLang} from '@jsverse/transloco-persist-lang';

import { routes } from './app.routes';
import type {LocaleCode} from '@cvhome-saas/ui-kit/i18n';
import {GlobalErrorHandler, apiErrorInterceptor, provideUiKit, withNotifications, withRequestContext} from '@cvhome-saas/ui-kit';
import {SelectedStoreRequestContext} from '@api/tenancy/selected-store-request-context';
import {BrowserLangStorage, LANG_COOKIE, LANG_STORAGE} from '@core/i18n/lang-storage';
import {LocaleService, StrictMissingHandler, TranslatedTitleStrategy} from '@cvhome-saas/ui-kit/i18n';
import {TranslocoDictionaryLoader} from '@core/i18n/transloco.loader';
import {provideTheme} from '@cvhome-saas/ui-kit/theme';
import {ToastService} from '@cvhome-saas/ui-kit/ui';
import {environment} from '@env/environment';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding()),
    provideClientHydration(withEventReplay()),
    provideHttpClient(withInterceptors([apiErrorInterceptor])),
    provideTheme(),
    provideUiKit(
      {
        apiUrl: environment.apiUrl,
        loginUrl: environment.loginUrl,
        logoutUrl: environment.logoutUrl,
        // The console reaches uaa through store-core-gateway, whose forward route is `/uaa`.
        uaaBasePath: '/uaa',
      },
      withNotifications(ToastService),
      // REQUEST_CONTEXT defaults to adding no parameters, which is right for a console with no
      // stores and wrong for this one: the implementation reads the store list, so it lives in the
      // api tier and is wired here.
      withRequestContext(SelectedStoreRequestContext),
    ),
    {provide: ErrorHandler, useClass: GlobalErrorHandler},
    {provide: LANG_STORAGE, useExisting: BrowserLangStorage},
    provideTransloco({
      config: {
        availableLangs: ['en', 'ar'],
        defaultLang: 'en',
        fallbackLang: 'en',
        reRenderOnLangChange: true,
        prodMode: !isDevMode(),
        missingHandler: {allowEmpty: false, logMissingKey: true, useFallbackTranslation: false},
      },
      loader: TranslocoDictionaryLoader,
    }),
    provideTranslocoMessageformat(),
    provideTranslocoLocale({langToLocaleMapping: {en: 'en-US', ar: 'ar-EG'}}),
    provideTranslocoPersistLang({storageKey: LANG_COOKIE, storage: {useExisting: LANG_STORAGE}}),
    provideTranslocoMissingHandler(StrictMissingHandler),
    {provide: TitleStrategy, useClass: TranslatedTitleStrategy},
    provideAppInitializer(() => {
      const transloco = inject(TranslocoService);
      const storage = inject(LANG_STORAGE);
      inject(LocaleService);

      // Resolved directly rather than left to transloco-persist-lang's own initializer:
      // that plugin only runs its lang-resolution step in the browser (see its
      // `isBrowser()` guard), so on the server it would always fall back to `defaultLang`
      // and never honour the cookie. Doing it here keeps server and client in lockstep,
      // which is what avoids a hydration mismatch.
      const cached = storage.getItem(LANG_COOKIE);
      if (cached === 'en' || cached === 'ar') {
        transloco.setActiveLang(cached satisfies LocaleCode);
      }
      return transloco.load(transloco.getActiveLang());
    }),
  ]
};
