export interface ProductGroupPage {
    totalPages: number
    number: number
    recordsTotal: number
    recordsFiltered: number
    products: Product[]
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
    images: Image2[]
    manufacturer: Manufacturer
    attributes: any[]
    options: any[]
    variants: any[]
    properties: any[]
    categories: Category[]
    type: any
    canBePurchased: boolean
    owner: any
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

export interface Description {
    id: number
    language: string
    name: string
    description: string
    friendlyUrl: string
    keyWords: string
    highlights: string
    metaDescription: string
    title: string
}

export interface ProductPrice {
    id: number
    originalPrice: string
    finalPrice: string
    discounted: boolean
    description: Description2
}

export interface Description2 {
    id: number
    language: string
    name: any
    description: any
    friendlyUrl: any
    keyWords: any
    highlights: any
    metaDescription: any
    title: any
    priceAppender: any
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

export interface Image2 {
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
    description: Description3
}

export interface Description3 {
    id: number
    language: any
    name: string
    description: any
    friendlyUrl: any
    keyWords: any
    highlights: any
    metaDescription: any
    title: any
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
    description: Description4
    productCount: number
    store: any
    children: any[]
}

export interface Parent {
    id: number
    code: string
}

export interface Description4 {
    id: number
    language: any
    name: string
    description: string
    friendlyUrl: string
    keyWords: any
    highlights: string
    metaDescription: string
    title: string
}
