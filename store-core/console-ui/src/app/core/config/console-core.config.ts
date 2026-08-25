import {InjectionToken} from '@angular/core';

export interface ConsoleCoreConfig {
  readonly apiUrl: string;
  readonly loginUrl: string;
  /** Where the gateway ends the session. Reached through the `external-logout-link` route, never linked directly. */
  readonly logoutUrl: string;
}

export const CONSOLE_CORE_CONFIG = new InjectionToken<ConsoleCoreConfig>('CONSOLE_CORE_CONFIG', {
  providedIn: 'root',
  factory: () => ({
    apiUrl: '',
    loginUrl: '/oauth2/authorization/uaa',
    logoutUrl: '/logout',
  }),
});
