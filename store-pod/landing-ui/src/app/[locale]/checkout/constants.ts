export const checkoutForm = {
    firstName: {
        name: "firstName",
        validate: {
            required: {
                value: true,
                message: "Firstname is required"
            }
        }
    },
    lastName: {
        name: "lastName",
        validate: {
            required: {
                value: true,
                message: "Lastname is required"
            }
        }
    },
    company: {
        name: "company"
    },
    address: {
        name: "address",
        validate: {
            required: {
                value: true,
                message: "Address is required"
            }
        }
    },
    city: {
        name: "city",
        validate: {
            required: {
                value: true,
                message: "City is required"
            }
        }
    },
    country: {
        name: "country",
        validate: {
            required: {
                value: true,
                message: "Country is required"
            }
        }
    },
    stateProvince: {
        name: "stateProvince",
        validate: {
            required: {
                value: true,
                message: "State is required"
            }
        }
    },
    postalCode: {
        name: "postalCode",
        validate: {
            required: {
                value: true,
                message: "Postal code is required"
            }
        }
    },
    phone: {
        name: "phone",
        validate: {
            required: {
                value: true,
                message: "Phone number is required"
            },
            minLength: {
                value: 10,
                message: "Enter a 10-digit number"
            }
        }
    },
    email: {
        name: "email",
        validate: {
            required: {
                value: true,
                message: "Email is required"
            },
            pattern: {
                value: /^([\w.%+-]+)@([\w-]+\.)+([\w]{2,})$/i,
                message: 'Please entered the valid email id'
            }
        }
    },
    shipFirstName: {
        name: "shipFirstName",
        validate: {
            required: {
                value: true,
                message: "Firstname is required"
            }
        }
    },
    shipLastName: {
        name: "shipLastName",
        validate: {
            required: {
                value: true,
                message: "Lastname is required"
            }
        }
    },
    shipCompany: {
        name: "shipCompany"
    },
    shipAddress: {
        name: "shipAddress",
        validate: {
            required: {
                value: true,
                message: "Address is required"
            }
        }
    },
    shipCity: {
        name: "shipCity",
        validate: {
            required: {
                value: true,
                message: "City is required"
            }
        }
    },
    shipCountry: {
        name: "shipCountry",
        validate: {
            required: {
                value: true,
                message: "Country is required"
            }
        }
    },
    shipStateProvince: {
        name: "shipStateProvince",
        validate: {
            required: {
                value: true,
                message: "State is required"
            }
        }
    },
    shipPostalCode: {
        name: "shipPostalCode",
        validate: {
            required: {
                value: true,
                message: "Postal code is required"
            }
        }
    },
    isAgree: {
        name: "isAgree",
        validate: {
            required: {
                value: true,
                message: "Please agree to our terms and conditions"
            }
        }
    },
    password: {
        name: "password",
        validate: {
            required: {
                value: true,
                message: "Password is required"
            },
            validate: {
                hasSpecialChar: (value) => (value && value.match(/^(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,16}$/)) || 'Password must be minimum of 8 characters atleast one number and one special character'
            }
        }
    },
    repeatPassword: {
        name: "repeatPassword",
        validate: {
            required: {
                value: true,
                message: "Repeat Password is required"
            }
        }
    }
}
