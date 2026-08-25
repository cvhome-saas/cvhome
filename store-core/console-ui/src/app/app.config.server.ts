import { mergeApplicationConfig, ApplicationConfig } from '@angular/core';
import { provideServerRendering, withRoutes } from '@angular/ssr';
import { appConfig } from './app.config';
import { serverRoutes } from './app.routes.server';
import { LANG_STORAGE, ServerLangStorage } from '@core/i18n/lang-storage';

const serverConfig: ApplicationConfig = {
  providers: [
    provideServerRendering(withRoutes(serverRoutes)),
    // The client's default reads document.cookie, which does not exist on the server;
    // this reads the same cookie off the incoming request instead.
    {provide: LANG_STORAGE, useExisting: ServerLangStorage},
  ]
};

export const config = mergeApplicationConfig(appConfig, serverConfig);
