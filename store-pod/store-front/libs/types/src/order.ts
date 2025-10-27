export interface Order {
    id: number
    billing: OrderAddress
    delivery: OrderAddress
    shipping: any
    payment: string
    total: OrderTotal | undefined
    products: ProductItem[] | undefined
}

export interface OrderAddress {
    postalCode: string
    countryCode: any
    firstName: string
    lastName: string
    bilstateOther: any
    company: string
    phone: string
    address: string
    city: string
    stateProvince: any
    billingAddress: boolean
    latitude: any
    longitude: any
    zone: any
    country: string
}

export interface OrderTotal {
    totals: TotalItem[] | undefined
    grandTotal: any
}

export interface TotalItem {
    id: number
    title: string
    text: any
    code: string
    order: number
    module: string
    value: number
    total: string
    discounted: boolean
}

export interface ProductItem {
    id: number
    sku: string
    orderedQuantity: number
    // product: Product
    productName: string
    price: string
    subTotal: string
    attributes: any[]
    image: any
}
