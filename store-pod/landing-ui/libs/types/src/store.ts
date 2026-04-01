import {ColorTheme} from "./color-schema";

export interface Store {
    id: string
    code: string
    name: string
    theme: Theme | undefined
    defaultLanguage: string
    currency: string
    inBusinessSince: string
    email: string
    phone: string
    template: any
    useCache: boolean
    requireLoginForOrderPlacement: boolean
    currencyFormatNational: boolean
    retailer: boolean
    dimension: string
    weight: string
    currentUserLanguage: any
    address: StoreAddress | undefined
    logo: ImageFile | undefined
    banner: ImageFile | undefined
    parent: any
    supportedLanguages: string[] | undefined
    readableAudit: ReadableAudit
    colorTheme: ColorTheme | undefined
    sliderImages: SliderImage[] | undefined
    socialLinks: SocialLink[] | undefined
}

export interface StoreAddress {
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
