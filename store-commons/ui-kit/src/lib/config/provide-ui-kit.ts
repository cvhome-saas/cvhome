import {makeEnvironmentProviders, type EnvironmentProviders, type Provider, type Type} from '@angular/core';

import {NOTIFICATION_PORT, type NotificationPort} from '../errors/notification.port';
import {REQUEST_CONTEXT, type RequestContextProvider} from '../http/request-context';
import {UI_KIT_CONFIG, type UiKitConfig} from './ui-kit.config';

/**
 * One optional piece of wiring. Angular's own `provideRouter(routes, withComponentInputBinding())`
 * shape: the features a consumer does not pass are the ones it does not need, and each carries its
 * own providers rather than the caller assembling them.
 */
export interface UiKitFeature {
  readonly providers: Provider[];
}

/**
 * Wires the kit into an application.
 *
 * ```ts
 * provideUiKit(
 *   {apiUrl: environment.apiUrl, loginUrl: environment.loginUrl, logoutUrl: environment.logoutUrl},
 *   withNotifications(ToastService),
 *   withRequestContext(SelectedStoreRequestContext),
 * )
 * ```
 */
export function provideUiKit(config: UiKitConfig, ...features: UiKitFeature[]): EnvironmentProviders {
  return makeEnvironmentProviders([
    {provide: UI_KIT_CONFIG, useValue: config},
    ...features.flatMap((feature) => feature.providers),
  ]);
}

/**
 * Where `ApiErrorService` sends the message it built.
 *
 * A port rather than a service because the two consumers raise a toast differently, and the error
 * stack — the `errors.code.x → errors.category.x → errors.generic` fallback chain — is the part
 * worth sharing, not the toast.
 */
export function withNotifications(implementation: Type<NotificationPort>): UiKitFeature {
  return {providers: [{provide: NOTIFICATION_PORT, useExisting: implementation}]};
}

/**
 * What scopes each request to a tenant. Omit it and requests carry no extra parameters, which is
 * correct for an application that has no notion of a selected store.
 */
export function withRequestContext(implementation: Type<RequestContextProvider>): UiKitFeature {
  return {providers: [{provide: REQUEST_CONTEXT, useExisting: implementation}]};
}
