import {ApplicationConfig, ErrorHandler, importProvidersFrom, provideZoneChangeDetection} from '@angular/core';

import {routes} from './app.routes';
import {provideHttpClient, withFetch, withInterceptors} from '@angular/common/http';
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
import {QuillModule} from 'ngx-quill';
import {DEFAULT_THEME} from "./pages/theme/styles/theme.default";
import {CookieService} from "ngx-cookie-service";
import {provideTranslateService} from '@ngx-translate/core';
import {provideAnimationsAsync} from "@angular/platform-browser/animations/async";
import {provideServerRendering, withRoutes} from "@angular/ssr";
import {serverRoutes} from "./app.routes.server";
import {provideRouter} from "@angular/router";
import {provideClientHydration} from "@angular/platform-browser";
import {apiErrorInterceptor} from "seller-core";
import {GlobalErrorHandler} from "seller-core";
import {provideSellerCore, StoreMode, withNotifications} from 'seller-core';
import {NotificationService} from './core/notifications/notification.service';
import {environment} from '../environments/environment';

export const appConfig: ApplicationConfig = {
  providers: [
    provideSellerCore({
      apiUrl: environment.apiUrl,
      loginUrl: environment.LOGIN_URL,
      logoutUrl: environment.LOGOUT_URL,
      mode: environment.mode as StoreMode,
      defaultStore: environment.defaultStore,
      languages: {default: environment.client.language.default, available: environment.client.language.array},
    }, withNotifications(NotificationService)),
    provideZoneChangeDetection({eventCoalescing: true}),
    provideRouter(routes),
    provideClientHydration(),
    provideServerRendering(withRoutes(serverRoutes)),
    // Every HTTP failure leaves this client as an ApiError, so no call site ever sees an
    // HttpErrorResponse again. Presentation stays a call-site decision; the interceptor never toasts.
    provideHttpClient(withFetch(), withInterceptors([apiErrorInterceptor])),
    {provide: ErrorHandler, useClass: GlobalErrorHandler},
    provideAnimationsAsync(),
    provideTranslateService({
      fallbackLang: 'en',
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
      NbChatModule.forRoot({
        messageGoogleMapKey: 'AIzaSyA_wNuCzia92MAmdLRzmqitRGvCF7wCZPY',
      }),
      QuillModule.forRoot(),
      CookieService
    ),
  ],
};
