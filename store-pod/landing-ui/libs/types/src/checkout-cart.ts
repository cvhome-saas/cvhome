import {Customer} from "./customer";


export interface CheckoutCart {
    paymentType: PaymentType
    customer: Customer
}

export enum PaymentType {
    COD = 'COD',
    STRIPE = 'STRIPE',
    PAYPAL = 'PAYPAL',
}