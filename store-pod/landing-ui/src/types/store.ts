import {ColorTheme} from "@/types/color-schema";

export interface Store {
    id: number
    code: string
    name: string
    theme: Theme
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
    logo: ImageFile
    banner: ImageFile
    parent: any
    supportedLanguages: SupportedLanguage[]
    readableAudit: ReadableAudit
    colorTheme: ColorTheme
    sliderImages: SliderImage[]
    socialLinks: SocialLink[]
}

export interface Address {
    stateProvince: string
    country: string
    address: string
    postalCode: string
    city: string
    active: boolean
}

export interface ImageFile {
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

export enum Theme {
    DEFAULT = 'DEFAULT',
    FASHION = 'FASHION',
    FURNITURE = 'FURNITURE',
    SPORTS = 'SPORTS',
    ELECTRONICS = 'ELECTRONICS',
    FOOD = 'FOOD',
    GLASSES = 'GLASSES',
    COSMETICS = 'COSMETICS',
    WATCHES = 'WATCHES',
    BABY = 'BABY',
    JEWELERY = 'JEWELERY',
    TOOLS = 'TOOLS',
}

export interface SliderImage {
    priority: number
    url: string
}

export interface SocialLink {
    provider: string
    url: string
}