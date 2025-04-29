import {Description} from "@/types/description";

export interface ProductGroupPage {
    totalPages: number
    number: number
    recordsTotal: number
    recordsFiltered: number
    products: Product[]
    productGroup: ProductGroup
}

export interface ProductGroup {
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
    productSpecifications: ProductSpecifications
    rating: number
    ratingCount: number
    refSku: string
    rentalDuration: number
    rentalPeriod: number
    description: Description
    productPrice: ProductPrice
    finalPrice: string
    originalPrice: string
    discounted: boolean
    image: Image
    images: Image[]
    manufacturer: Manufacturer
    attributes: any[]
    options: any[]
    variants: any[]
    properties: any[]
    categories: Category[]
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


export interface Category {
    id: number
    code: string
    sortOrder: number
    visible: boolean
    featured: boolean
    lineage: string
    depth: number
    parent?: Parent
    description: Description
    productCount: number
    store: any
    children: any[]
}

export interface Parent {
    id: number
    code: string
}
