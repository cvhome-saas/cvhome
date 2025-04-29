import {ApplicationConfig, importProvidersFrom, provideZoneChangeDetection} from '@angular/core';
import {provideRouter} from '@angular/router';

import {routes} from './app.routes';
import {BrowserModule, provideClientHydration} from '@angular/platform-browser';
import {HttpClient, provideHttpClient, withFetch} from '@angular/common/http';
import {BrowserAnimationsModule, provideAnimations} from '@angular/platform-browser/animations';
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
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import {QuillModule} from 'ngx-quill';
import {DEFAULT_THEME} from "./theme/styles/theme.default";
import {FilePickerModule} from "ngx-awesome-uploader";
import {CookieService} from "ngx-cookie-service";

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({eventCoalescing: true}),
    provideRouter(routes),
    provideClientHydration(),
    provideHttpClient(withFetch()),
    provideAnimations(),
    importProvidersFrom(
      BrowserModule,
      BrowserAnimationsModule,
      TranslateModule.forRoot({
        loader: {
          provide: TranslateLoader,
          useFactory: HttpLoaderFactory,
          deps: [HttpClient]
        }
      }),
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
      FilePickerModule,
      CookieService
    ),
  ],
};

export function HttpLoaderFactory(http: HttpClient): TranslateHttpLoader {
  return new TranslateHttpLoader(http);
}
