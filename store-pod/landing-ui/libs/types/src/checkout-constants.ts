import {PaymentType} from "./checkout-cart";

export const defaultCheckoutValue = {
    paymentType: PaymentType.COD,
    customer: {
        emailAddress: "",
        billing: {
            firstName: "",
            lastName: "",
            phone: "",
            country: "",
            city: "",
            postalCode: "",
            address: "",
            isAgree: false,
        },
    }
};
