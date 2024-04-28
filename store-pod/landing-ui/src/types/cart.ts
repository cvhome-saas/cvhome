export interface Cart {
    id: number
    language: any
    code: string
    subtotal: number
    displaySubTotal: string
    total: number
    displayTotal: string
    quantity: number
    order: any
    promoCode: any
    variant: any
    products: Product[]
    totals: Total[]
    customer: any
}

export interface Product {
    id: number
    productShipeable: boolean
    available: boolean
    visible: boolean
    sortOrder: number
    dateAvailable: string
    creationDate: any
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
    productPrice: any
    finalPrice: string
    originalPrice: string
    image: Image
    images: Image2[]
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

export interface Description {
    id: number
    language: string
    name: string
    description: string
    friendlyUrl: any
    keyWords: any
    highlights: any
    metaDescription: any
    title: any
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

export interface Total {
    id: number
    title: any
    text: any
    code: string
    order: number
    module: any
    value: number
    total: any
    discounted: boolean
}
