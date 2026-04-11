export interface Customer {
    id: number
    cuaExternalId: string
    emailAddress: string
    firstNames: string
    lastName: string
    username: string
    billing: Address
    delivery: Address
}

export interface Address {
    address: string
    city: string
    company: string
    firstName: string
    isAgree: boolean
    lastName: string
    phone: string
    postalCode: string,
    country: string

}
