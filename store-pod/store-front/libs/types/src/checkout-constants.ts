export const defaultCheckoutValue = {
    payment: {
        paymentType: "COD",
        transactionType: "INIT",
    },
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
