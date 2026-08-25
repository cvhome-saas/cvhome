import {Description} from "./description";
import {Category} from "./category";

/** Page of products as the catalog returns it (`/api/v2/products`): `pageNumber`/`size`/`totalElements` are
 *  the live fields; `number`/`recordsTotal` are kept for older payloads. */
export interface ProductGroupPage {
    totalPages: number
    number?: number
    pageNumber?: number
    size?: number
    totalElements?: number
    recordsTotal?: number
    recordsFiltered: number
    content: Product[] | undefined
    productGroup: ProductGroup | undefined
}

export interface ProductGroup {
    description: Description | undefined
    products: Product[] | undefined
    code: string
    active: boolean
    id: number
}

export interface Product {
    id: number
    productShipeable: boolean
    available: boolean
    visible: boolean
    sortOrder: number
    dateAvailable: string
    creationDate: string
    price: number
    quantity: number
    sku: string
    preOrder: boolean
    productVirtual: boolean
    quantityOrderMaximum: number
    quantityOrderMinimum: number
    productIsFree: boolean
    productSpecifications: ProductSpecifications | undefined
    rating: number
    ratingCount: number
    refSku: string
    rentalDuration: number
    rentalPeriod: number
    description: Description | undefined
    productPrice: ProductPrice | undefined
    finalPrice: string
    originalPrice: string
    discounted: boolean
    image: Image | undefined
    images: Image[] | undefined
    manufacturer: Manufacturer | undefined
    attributes: ProductAttribute[] | undefined
    options: ProductOption[] | undefined
    variants: ProductVariant[] | undefined
    properties: ProductProperty[] | undefined
    categories: Category[] | undefined
    type: any
    canBePurchased: boolean
    owner: any
    subTotal: number
    displaySubTotal: string
    cartItemattributes: any[]
    variant: any
    variantValue: any
}

export interface ProductSpecifications {
    height: any
    weight: any
    length: any
    width: any
    model: any
    manufacturer: any
    dimensionUnitOfMeasure: any
    weightUnitOfMeasure: any
}


export interface ProductPrice {
    id: number
    originalPrice: string
    finalPrice: string
    discounted: boolean
    description: Description
}

export interface Image {
    id: number
    imageName: string
    imageUrl: string
    externalUrl: any
    videoUrl: any
    imageType: number
    order: number
    defaultImage: boolean
}


export interface Manufacturer {
    id: number
    code: string
    order: number
    description: Description
}

export interface Parent {
    id: number
    code: string
}

/** A product group code the home page renders. */
export type ProductGroupCode = 'FEATURED_ITEMS' | 'NEWLY_ADDED' | 'HOME_PAGE' | 'RECOMMENDED';

/* ---- options / variants / attributes (mirror catalog ReadableProductOption & co.) ---------------- */

/** e.g. "Color" / "Size". `variant === true` means the choice selects a sellable variant (own SKU). */
export interface ProductOption {
    id: number
    code: string
    type: string | undefined
    readOnly: boolean
    name: string
    lang: string | undefined
    variant: boolean
    optionValues: ProductOptionValue[]
}

export interface ProductOptionValue {
    id: number
    code: string
    name: string | undefined
    /** Pre-formatted surcharge/price for this value, if the catalog defines one. */
    price: string | undefined
    image: string | undefined
    description: string | undefined
    sortOrder: number | undefined
    defaultValue: boolean | undefined
}

export interface ProductVariation {
    id: number
    code: string | undefined
    option: ProductOption | undefined
    optionValue: ProductOptionValue | undefined
}

export interface ProductVariantInventory {
    sku: string
    /** Pre-formatted. */
    price: string | undefined
    prices: ProductPrice[] | undefined
    quantity: number | undefined
}

/** A sellable combination (own SKU, images, stock, price). */
export interface ProductVariant {
    id: number
    sku: string
    code: string
    available: boolean
    sortOrder: number
    defaultSelection: boolean
    variation: ProductVariation | undefined
    variationValue: ProductVariation | undefined
    images: Image[] | undefined
    inventory: ProductVariantInventory[] | undefined
}

/** Descriptive attribute ("Material: cotton"); never affects the SKU. */
export interface ProductAttribute {
    id: number
    code: string
    name: string
    lang: string | undefined
    type: string | undefined
    attributeValues: ProductAttributeValue[]
}

export interface ProductAttributeValue {
    id: number
    code: string
    name: string | undefined
    lang: string | undefined
    description: string | undefined
}

export interface ProductProperty {
    id: number
    code: string
    property: ProductOption | undefined
    propertyValue: ProductOptionValue | undefined
}

/** `{option: optionId, value: optionValueId}` pairs sent to the variation price endpoint. */
export interface SelectedVariantValue {
    option: number
    value: number
}
