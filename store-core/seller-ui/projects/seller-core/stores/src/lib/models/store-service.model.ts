import {ManagerStoreDomain, ReadableMerchantStore} from './store';
import {SpringPage} from 'seller-core';

export type {SpringPage};

/** tenancy StoreManagerServiceImpl#getStore forwards to the merchant
 *  pod's ReadableMerchantStore and merges in a `pod` key — not a separate
 *  Java DTO, confirmed by reading the actual implementation rather than the
 *  controller's declared `Object` return type. */
export interface ReadableMerchantStoreWithPod extends ReadableMerchantStore {
  pod: {id: string};
}

/** Local shape of the store-scoped "page content" (e.g. LANDING_PAGE) fetched
 *  via getPageContent/updatePageContent/createPageContent — no matching
 *  controller was found for `/spg/merchant/api/v1/private/content/any/{code}`
 *  or the bare `/private/content` POST/PUT (ContentApi only exposes
 *  `/private/content/box` and `/private/content/page`); typed from what
 *  store-landing-page.facade.ts and its form service already read/write
 *  rather than a verified Java contract. */
export interface LandingPageContent {
  status?: number;
  id?: number;
  code?: string;
  name?: string;
  descriptions?: {
    id?: number;
    language?: string;
    name?: string;
    metaDescription?: string;
    keyWords?: string;
    description?: string;
  }[];
}

/** Mirrors tenancy manager/controller/SaasController#saasProperties */
export interface SaasProperties {
  alis: string;
  domain: string;
}

/** Mirrors merchant-commons model/merchant/PersistableMerchantStore -> MerchantStoreEntity */
export interface PersistableMerchantStore {
  [key: string]: unknown;
  id: string;
  currencyFormatNational?: boolean;
  currency?: string;
  defaultLanguage?: string;
  countryIsoCode?: string;
  name?: string;
  org?: string;
  theme?: string;
  colorTheme?: string;
  inBusinessSince?: string;
  email?: string;
  phone?: string;
  template?: string;
  useCache?: boolean;
  requireLoginForOrderPlacement?: boolean;
  dimension?: string;
  weight?: string;
  address?: Address;
  retailerStore?: string;
  supportedLanguages?: string[];
  socialLinks?: SocialLink[];
  sliderImages?: {priority: number; name: string}[];
  storeDomains?: ManagerStoreDomain[];
}

/** Mirrors reference-commons model/references/PersistableBaseAddress -> BaseAddress */
export interface Address {
  stateProvince?: string;
  country?: string;
  address?: string;
  postalCode?: string;
  city?: string;
  active?: boolean;
}

/** Mirrors commons/domain/SocialLink (record) */
export interface SocialLink {
  provider: string;
  url: string;
}

/** Mirrors cua web/dto/ReadableSocialLoginConfig */
export interface ReadableSocialLoginConfig {
  storeMerchantId?: unknown;
  providerId?: string;
  appId?: string;
  appSecret?: string;
  enabled?: boolean;
}

/** Mirrors cua web/dto/PersistableSocialLoginConfig */
export interface PersistableSocialLoginConfig {
  storeMerchantId?: unknown;
  providerId?: string;
  appId?: string;
  appSecret?: string;
  enabled?: boolean;
}

/** Mirrors payment-core models/ReadablePaymentConfiguration */
export interface ReadablePaymentConfiguration {
  storeMerchantId?: unknown;
  paymentType?: string;
  apiKey?: string;
  secretKey?: string;
  webhookSecret?: string;
  enabled?: boolean;
}

/** Mirrors payment-core models/PersistablePaymentConfiguration */
export interface PersistablePaymentConfiguration {
  storeMerchantId?: unknown;
  paymentType?: string;
  apiKey?: string;
  secretKey?: string;
  webhookSecret?: string;
  enabled?: boolean;
}

