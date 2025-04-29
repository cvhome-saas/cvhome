import {Product} from "@/types/product-groups";

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
