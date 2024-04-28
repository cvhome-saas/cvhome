export interface Store {
  id: number
  code: string
  name: string
  defaultLanguage: string
  currency: string
  inBusinessSince: string
  email: string
  phone: string
  template: any
  useCache: boolean
  currencyFormatNational: boolean
  retailer: boolean
  dimension: string
  weight: string
  currentUserLanguage: any
  address: Address
  logo: Logo
  parent: any
  supportedLanguages: SupportedLanguage[]
  readableAudit: ReadableAudit
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

export interface SupportedLanguage {
  code: string
  id: number
}

export interface ReadableAudit {
  created: any
  modified: string
  user: string
}
