import {ColorTheme} from "./color-schema";

/**
 * The store record: configuration, not appearance.
 *
 * Its logo, banner, slider images and social links used to live here. They moved to the content service, which
 * owns the media library they come from, and reach the themes through `LayoutData` instead.
 */
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
    parent: any
    supportedLanguages: string[] | undefined
    readableAudit: ReadableAudit
    colorTheme: ColorTheme | undefined
}

export interface StoreAddress {
    stateProvince: string
    country: string
    address: string
    postalCode: string
    city: string
    active: boolean
}


export interface ReadableAudit {
    created: any
    modified: string
    user: string
}

export enum Theme {
    BASIS = 'BASIS',
    MODERN = 'MODERN',
    JEWELERY = 'JEWELERY',
    BEAUTY = 'BEAUTY',
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
    TOOLS = 'TOOLS',
    BASIC = 'BASIC',
    GROCERY = 'GROCERY',
    PINK = 'PINK',
    HUNGER = 'HUNGER',
    JEWELLERY = 'JEWELLERY',
}

