import {InjectionToken} from '@angular/core';

/**
 * What a consuming application has to tell the kit about its deployment.
 *
 * Deliberately three fields. A library cannot read an app's `environment.ts`, and the temptation is
 * to widen this into a bag holding everything an app might configure; every field added here is one
 * more thing a second consumer has to answer before it can call an endpoint.
 */
export interface UiKitConfig {
  /** Prefix for every request `CrudService` makes. Empty when the app is served by its own gateway. */
  readonly apiUrl: string;
  readonly loginUrl: string;
  /** Where the gateway ends the session. Reached through the `external-logout-link` route, never linked directly. */
  readonly logoutUrl: string;
}

/**
 * Defaulted rather than required, so a consumer that has not called `provideUiKit` still boots — and
 * boots against its own origin, which is what both apps here actually want.
 */
export const UI_KIT_CONFIG = new InjectionToken<UiKitConfig>('UI_KIT_CONFIG', {
  providedIn: 'root',
  factory: () => ({
    apiUrl: '',
    loginUrl: '/oauth2/authorization/uaa',
    logoutUrl: '/logout',
  }),
});
