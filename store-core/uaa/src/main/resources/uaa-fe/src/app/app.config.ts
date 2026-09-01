import {
  ApplicationConfig,
  ErrorHandler,
  isDevMode,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
} from '@angular/core';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {TitleStrategy, provideRouter, withComponentInputBinding} from '@angular/router';
import {provideTransloco, provideTranslocoMissingHandler} from '@jsverse/transloco';
import {provideTranslocoLocale} from '@jsverse/transloco-locale';
import {provideTranslocoMessageformat} from '@jsverse/transloco-messageformat';

import {
  GlobalErrorHandler,
  apiErrorInterceptor,
  provideUiKit,
  withNotifications,
} from '@cvhome-saas/ui-kit';
import {StrictMissingHandler, TranslatedTitleStrategy} from '@cvhome-saas/ui-kit/i18n';
import {provideTheme} from '@cvhome-saas/ui-kit/theme';
import {ToastService} from '@cvhome-saas/ui-kit/ui';

import {routes} from './app.routes';
import {TranslocoDictionaryLoader} from '@core/i18n/transloco.loader';
import {environment} from '@env/environment';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({eventCoalescing: true}),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([apiErrorInterceptor])),
    provideTheme(),
    /*
     * No `withRequestContext(...)`, deliberately. REQUEST_CONTEXT defaults to adding no parameters,
     * and that is the right answer here: uaa is platform-wide and has no notion of a selected store,
     * so a `?store=` on an admin call would be meaningless. console-ui overrides it because it does.
     */
    provideUiKit(
      {
        apiUrl: environment.apiUrl,
        loginUrl: environment.loginUrl,
        logoutUrl: environment.logoutUrl,
      },
      withNotifications(ToastService),
    ),
    {provide: ErrorHandler, useClass: GlobalErrorHandler},
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
    provideTranslocoMissingHandler(StrictMissingHandler),
    // Route `titleKey`s are inert without this: the strategy is what turns one into a tab title.
    {provide: TitleStrategy, useClass: TranslatedTitleStrategy},
  ],
};
