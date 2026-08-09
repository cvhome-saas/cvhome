import {EnvironmentProviders, makeEnvironmentProviders, Provider, Type} from '@angular/core';
import {NOTIFICATION_PORT, NotificationPort} from '../errors/notification.port';
import {REQUEST_CONTEXT, RequestContextProvider} from '../http/request-context';
import {SELLER_CORE_CONFIG, SellerCoreConfig} from './seller-core.config';

export interface SellerCoreFeature { providers: Provider[]; }

export function provideSellerCore(config: SellerCoreConfig, ...features: SellerCoreFeature[]): EnvironmentProviders {
  return makeEnvironmentProviders([
    {provide: SELLER_CORE_CONFIG, useValue: config},
    ...features.flatMap(feature => feature.providers),
  ]);
}

export function withNotifications(implementation: Type<NotificationPort>): SellerCoreFeature {
  return {providers: [{provide: NOTIFICATION_PORT, useExisting: implementation}]};
}

export function withRequestContext(implementation: Type<RequestContextProvider>): SellerCoreFeature {
  return {providers: [{provide: REQUEST_CONTEXT, useExisting: implementation}]};
}
