export interface Store {
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
  template: any
  useCache: boolean
  requireLoginForOrderPlacement: boolean
  dimension: string
  weight: string
  currentUserLanguage: any
  address: Address
  logo: Logo
  banner: Banner
  audit: any
  parent: any
  supportedLanguages: SupportedLanguage[]
  readableAudit: any
}

export interface SocialLink {
  provider: string
  url: string
}

export interface SliderImage {
  priority: number
  url: string
}

export interface Address {
  stateProvince: string
  country: string
  address: string
  postalCode: string
  city: string
  active: boolean
}

export interface Logo {
  name: string
  path: string
}

export interface Banner {
  name: string
  path: string
}

export interface SupportedLanguage {
  code: string
  id: string
}
