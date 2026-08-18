import {InjectionToken} from '@angular/core';

export interface ConsoleCoreConfig {
  readonly apiUrl: string;
  readonly loginUrl: string;
}

export const CONSOLE_CORE_CONFIG = new InjectionToken<ConsoleCoreConfig>('CONSOLE_CORE_CONFIG', {
  providedIn: 'root',
  factory: () => ({
    apiUrl: '',
    loginUrl: '/oauth2/authorization/uaa',
  }),
});
