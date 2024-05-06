export interface Customer {
    emailAddress: string
    billing: Billing
}

export interface Billing {
    address: string
    city: string
    company: string
    firstName: string
    isAgree: boolean
    lastName: string
    phone: string
    postalCode: string,
    country:string

}

export interface payment {
    paymentType: string
    transactionType: string
    paymentModule: string
    amount: number
}

export interface CheckoutCart {
    currency: string
    payment: payment
    customer: Customer
}