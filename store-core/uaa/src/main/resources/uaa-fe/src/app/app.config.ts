import {ApplicationConfig, importProvidersFrom, provideZoneChangeDetection} from '@angular/core';

import {routes} from './app.routes';
import {provideHttpClient, withFetch} from '@angular/common/http';
import {
  NbChatModule,
  NbDatepickerModule,
  NbDialogModule,
  NbMenuModule,
  NbSidebarModule,
  NbThemeModule,
  NbToastrModule,
  NbWindowModule
} from '@nebular/theme';
import {provideTranslateHttpLoader} from '@ngx-translate/http-loader';
import {DEFAULT_THEME} from "./pages/home/theme/styles/theme.default";
import {CookieService} from "ngx-cookie-service";
import {provideTranslateService} from '@ngx-translate/core';
import {provideAnimationsAsync} from "@angular/platform-browser/animations/async";
import {provideRouter} from "@angular/router";

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({eventCoalescing: true}),
    provideRouter(routes),
    provideHttpClient(withFetch()),
    provideAnimationsAsync(),
    provideTranslateService({
      defaultLanguage: 'en',
      loader: provideTranslateHttpLoader({
        prefix: './assets/i18n/',
        suffix: '.json'
      }),
    }),

    importProvidersFrom(
      NbThemeModule.forRoot({
          name: 'default',
        },
        [DEFAULT_THEME]
      ),
      // NbEvaIconsModule,
      NbSidebarModule.forRoot(),
      NbMenuModule.forRoot(),
      NbDatepickerModule.forRoot(),
      NbDialogModule.forRoot(),
      NbWindowModule.forRoot(),
      NbToastrModule.forRoot(),
      CookieService
    ),
  ],
};
