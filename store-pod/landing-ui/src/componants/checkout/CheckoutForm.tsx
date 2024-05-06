'use client'
import {Cart} from "@/types/cart";
import {checkoutForm} from "@/componants/checkout/constants";
import React, {Suspense} from "react";
import {CheckoutCartDetails} from "@/componants/checkout/CheckoutCartDetails";
import {FieldValues, useForm} from "react-hook-form"
import {CheckoutCart} from "@/types/checkout-cart";
import {CartService} from "@/services/cart-service";
import {StoreContext} from "@/types/store-context";

export const CheckoutForm = ({storeContext, cart, agreement, t}: {
    storeContext: StoreContext,
    cart: Cart, agreement: string,
    t: { [key: string]: string }
}) => {
    // const errors: { [key: string]: any } = {};
    const {
        register,
        handleSubmit,
        watch,
        formState: {errors},
    } = useForm()
    return <form onSubmit={handleSubmit((data: FieldValues) => {
        let checkoutCart: CheckoutCart = {
            currency: "CAD",
            payment: {
                "paymentType": "CREDITCARD",
                "transactionType": "CAPTURE",
                "paymentModule": "stripe",
                "amount": cart.total
            },
            customer: {
                emailAddress: data.email,
                billing: {
                    country:"CA",
                    address: data.address,
                    city: data.city,
                    company: data.company,
                    firstName: data.firstName,
                    isAgree: data.isAgree,
                    lastName: data.lastName,
                    phone: data.phone,
                    postalCode: data.postalCode
                }
            }
        }
        CartService.checkout(storeContext, cart.code, checkoutCart).then(it => {
            console.log(it)
        })
    })}>
        <div className="row">

            <div className="col-lg-6">
                <div className="billing-info-wrap">
                    <h3>{t["Billing Details"]}</h3>
                    <div className="row">
                        <div className="col-lg-6 col-md-6">
                            <div className="billing-info mb-20">
                                <label>{t["First Name"]}</label>
                                <input type="text"
                                       {...register(checkoutForm.firstName.name, checkoutForm.firstName.validate)} />
                                {errors[checkoutForm.firstName.name] &&
                                    <p className="error-msg">
                                        {`${errors[checkoutForm.firstName.name]?.message}`}
                                    </p>}
                            </div>
                        </div>
                        <div className="col-lg-6 col-md-6">
                            <div className="billing-info mb-20">
                                <label>{t["Last Name"]}</label>
                                <input
                                    type="text"  {...register(checkoutForm.lastName.name, checkoutForm.lastName.validate)}/>
                                {errors[checkoutForm.lastName.name] &&
                                    <p className="error-msg">{`${errors[checkoutForm.lastName.name]?.message}`}</p>}
                            </div>
                        </div>
                        <div className="col-lg-12">
                            <div className="billing-info mb-20">
                                <label>{t["Company Name"]}</label>
                                <input type="text" {...register(checkoutForm.company.name)}/>
                            </div>
                        </div>

                        <div className="col-lg-12">
                            <div className="billing-info mb-20">
                                <label>{t["Street Address"]}</label>
                                <input
                                    className="billing-address"
                                    placeholder={t["House number and street name"]}
                                    type="text"
                                    id="autocomplete" {...register(checkoutForm.address.name, checkoutForm.address.validate)}
                                />
                                {errors[checkoutForm.address.name] &&
                                    <p className="error-msg">{`${errors[checkoutForm.address.name]?.message}`}</p>}

                            </div>
                        </div>
                        <div className="col-lg-12">
                            <div className="billing-select mb-20">
                                <label>{t["Country"]}</label>


                                {errors[checkoutForm.country.name] &&
                                    <p className="error-msg">{`${errors[checkoutForm.country.name]?.message}`}</p>}
                            </div>
                        </div>
                        <div className="col-lg-12">
                            <div className="billing-info mb-20">
                                <label>{t["Town/City"]}</label>
                                <input type="text" {...register(checkoutForm.city.name, checkoutForm.city.validate)}/>
                                {errors[checkoutForm.city.name] &&
                                    <p className="error-msg">{`${errors[checkoutForm.city.name]?.message}`}</p>}
                            </div>
                        </div>
                        <div className="col-lg-6 col-md-6">
                            <div className="billing-select mb-20">
                                <label>{t["State"]}</label>
                                {errors[checkoutForm.stateProvince.name] &&
                                    <p className="error-msg">{`${errors[checkoutForm.stateProvince.name]?.message}`}</p>}

                            </div>
                        </div>
                        <div className="col-lg-6 col-md-6">
                            <div className="billing-info mb-20">
                                <label>{t["Postcode"]}</label>
                                <input
                                    type="text" {...register(checkoutForm.postalCode.name, checkoutForm.postalCode.validate)}/>
                                {errors[checkoutForm.postalCode.name] &&
                                    <p className="error-msg">{`${errors[checkoutForm.postalCode.name]?.message}`}</p>}
                            </div>
                        </div>
                        <div className="col-lg-6 col-md-6">
                            <div className="billing-info mb-20">
                                <label>{t["Phone"]}</label>
                                <input type="text" {...register(checkoutForm.phone.name, checkoutForm.phone.validate)}/>
                                {errors[checkoutForm.phone.name] &&
                                    <p className="error-msg">{`${errors[checkoutForm.phone.name]?.message}`}</p>}
                            </div>
                        </div>
                        <div className="col-lg-6 col-md-6">
                            <div className="billing-info mb-20">
                                <label>{t["Email address"]}</label>
                                <input type="text" {...register(checkoutForm.email.name, checkoutForm.email.validate)}/>
                                {errors[checkoutForm.email.name] &&
                                    <p className="error-msg">{`${errors[checkoutForm.email.name]?.message}`}</p>}
                            </div>
                        </div>
                    </div>


                    <div className="additional-info-wrap">
                        <h4>{t["Additional information"]}</h4>
                        <div className="additional-info">
                            <label>{t["Order notes"]}</label>
                            <textarea
                                placeholder={t["Notes about your order, special notes for delivery"]}
                                name="message"
                                defaultValue={""}
                            />
                        </div>
                    </div>
                </div>
            </div>


            <div className="col-lg-6">
                <div className="your-order-area">
                    <h3>{t["Your order"]}</h3>
                    <CheckoutCartDetails cart={cart} t={t}/>
                    <div className="payment-method mt-25">

                        <div className="place-order mt-100">
                            <div className="login-toggle-btn mb-20">
                                <input
                                    type="checkbox" {...register(checkoutForm.isAgree.name, checkoutForm.isAgree.validate)}/>
                                <label
                                    className="ml-10 ">{t["I agree with the terms and conditions"]}</label>
                                {errors[checkoutForm.isAgree.name] &&
                                    <p className="error-msg">{`${errors[checkoutForm.isAgree.name]?.message}`}</p>}
                            </div>
                            <div>
                                <Suspense>
                                    <div className="agreement-info-wrap"
                                         dangerouslySetInnerHTML={{__html: agreement}}>
                                    </div>
                                </Suspense>
                            </div>
                            <button type="submit"
                                    className="btn-hover">{t["Place your order"]}</button>
                        </div>
                    </div>

                </div>
            </div>
        </div>
    </form>
}