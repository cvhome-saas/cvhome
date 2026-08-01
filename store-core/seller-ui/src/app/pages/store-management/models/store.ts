/** Mirrors merchant-commons model/merchant/ReadableMerchantStore -> MerchantStoreEntity
 *  -> MerchantStorePricingBase. Renamed from the app's original `Store` name
 *  (shared/models/commons.ts has an unrelated `Store` for the control-plane
 *  ManagerStoreDto — that one is untouched). */
export interface ReadableMerchantStore {
  id: string
  currencyFormatNational: boolean
  currency: string
  defaultLanguage: string
  countryIsoCode: string
  name: string
  org: string
  theme: string
  colorTheme: string
  socialLinks: SocialLink[]
  sliderImages: SliderImage[]
  inBusinessSince: string
  email: string
  phone: string
  template: string
  useCache: boolean
  requireLoginForOrderPlacement: boolean
  dimension: string
  weight: string
  currentUserLanguage: string
  address: Address
  logo: Logo
  banner: Banner
  audit: ReadableAudit
  parent: ReadableMerchantStore
  storeDomains: ManagerStoreDomain[]
  /** Mirrors List<String> supportedLanguages — plain language codes, not objects. */
  supportedLanguages: string[]
  readableAudit: ReadableAudit
}

/** Mirrors commons/domain/SocialLink (record) */
export interface SocialLink {
  provider: string
  url: string
}

/** Mirrors commons/domain/ReadableSliderImage (record) */
export interface SliderImage {
  priority: number
  name: string
  url: string
}

/** Mirrors reference-commons model/references/ReadableBaseAddress -> BaseAddress */
export interface Address {
  stateProvince: string
  country: string
  address: string
  postalCode: string
  city: string
  active: boolean
}

/** Mirrors store-commons model/content/ReadableImage */
export interface Logo {
  name: string
  path: string
}

/** Mirrors store-commons model/content/ReadableImage */
export interface Banner {
  name: string
  path: string
}

/** Mirrors store-commons model/entity/ReadableAudit */
export interface ReadableAudit {
  created: string
  modified: string
  user: string
}

/** Mirrors commons/domain/ManagerStoreDomain (record) */
export interface ManagerStoreDomain {
  domain: string
  domainType: string
}

