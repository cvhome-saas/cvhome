import {InjectionToken} from '@angular/core';

export type StoreMode = 'MARKETPLACE' | 'BTB' | 'STANDARD';

export interface SellerCoreConfig {
  apiUrl: string;
  loginUrl: string;
  logoutUrl: string;
  mode: StoreMode;
  defaultStore: string;
  languages: { default: string; available: readonly string[] };
}

export const SELLER_CORE_CONFIG = new InjectionToken<SellerCoreConfig>('SELLER_CORE_CONFIG');
