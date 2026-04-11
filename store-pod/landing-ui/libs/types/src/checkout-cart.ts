import {Customer} from "./customer";

export interface payment {
    paymentType: string
    transactionType: string
}

export interface CheckoutCart {
    payment: payment
    customer: Customer
}
