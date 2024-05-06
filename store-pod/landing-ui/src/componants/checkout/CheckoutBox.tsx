'use client'
import {checkoutForm} from "@/app/[locale]/checkout/constants";
import React, {Suspense} from "react";
import {Link} from "@/navigation";
import {Cart} from "@/types/cart";

export const CheckoutBox = ({cart, agreement, t}: {
    cart: Cart | undefined, agreement: string,
    t: { [key: string]: string }
}) => {
    const errors:{ [key: string]: any } = {};

    return <div className="checkout-area pt-95 pb-100">
        <div className="container">
            {cart && cart.code && cart.products.length > 0 ? (
                <form>
                    <div className="row">

                        <div className="col-lg-6">
                            <div className="billing-info-wrap">
                                <h3>{t["Billing Details"]}</h3>
                                <div className="row">
                                    <div className="col-lg-6 col-md-6">
                                        <div className="billing-info mb-20">
                                            <label>{t["First Name"]}</label>
                                            <input type="text" name={checkoutForm.firstName.name}/>
                                            {errors[checkoutForm.firstName.name] &&
                                                <p className="error-msg">{errors[checkoutForm.firstName.name].message}</p>}
                                        </div>
                                    </div>
                                    <div className="col-lg-6 col-md-6">
                                        <div className="billing-info mb-20">
                                            <label>{t["Last Name"]}</label>
                                            <input type="text" name={checkoutForm.lastName.name}/>
                                            {errors[checkoutForm.lastName.name] &&
                                                <p className="error-msg">{errors[checkoutForm.lastName.name].message}</p>}
                                        </div>
                                    </div>
                                    <div className="col-lg-12">
                                        <div className="billing-info mb-20">
                                            <label>{t["Company Name"]}</label>
                                            <input type="text" name={checkoutForm.company.name}/>
                                        </div>
                                    </div>

                                    <div className="col-lg-12">
                                        <div className="billing-info mb-20">
                                            <label>{t["Street Address"]}</label>
                                            <input
                                                className="billing-address"
                                                placeholder={t["House number and street name"]}
                                                type="text"
                                                id="autocomplete"
                                                name={checkoutForm.address.name}
                                            />
                                            {errors[checkoutForm.address.name] &&
                                                <p className="error-msg">{errors[checkoutForm.address.name].message}</p>}

                                        </div>
                                    </div>
                                    <div className="col-lg-12">
                                        <div className="billing-select mb-20">
                                            <label>{t["Country"]}</label>


                                            {errors[checkoutForm.country.name] &&
                                                <p className="error-msg">{errors[checkoutForm.country.name].message}</p>}
                                        </div>
                                    </div>
                                    <div className="col-lg-12">
                                        <div className="billing-info mb-20">
                                            <label>{t["Town/City"]}</label>
                                            <input type="text" name={checkoutForm.city.name}/>
                                            {errors[checkoutForm.city.name] &&
                                                <p className="error-msg">{errors[checkoutForm.city.name].message}</p>}
                                        </div>
                                    </div>
                                    <div className="col-lg-6 col-md-6">
                                        <div className="billing-select mb-20">
                                            <label>{t["State"]}</label>
                                            {errors[checkoutForm.stateProvince.name] &&
                                                <p className="error-msg">{errors[checkoutForm.stateProvince.name].message}</p>}

                                        </div>
                                    </div>
                                    <div className="col-lg-6 col-md-6">
                                        <div className="billing-info mb-20">
                                            <label>{t["Postcode"]}</label>
                                            <input type="text" name={checkoutForm.postalCode.name}/>
                                            {errors[checkoutForm.postalCode.name] &&
                                                <p className="error-msg">{errors[checkoutForm.postalCode.name].message}</p>}
                                        </div>
                                    </div>
                                    <div className="col-lg-6 col-md-6">
                                        <div className="billing-info mb-20">
                                            <label>{t["Phone"]}</label>
                                            <input type="text" name={checkoutForm.phone.name}/>
                                            {errors[checkoutForm.phone.name] &&
                                                <p className="error-msg">{errors[checkoutForm.phone.name].message}</p>}
                                        </div>
                                    </div>
                                    <div className="col-lg-6 col-md-6">
                                        <div className="billing-info mb-20">
                                            <label>{t["Email address"]}</label>
                                            <input type="text" name={checkoutForm.email.name}/>
                                            {errors[checkoutForm.email.name] &&
                                                <p className="error-msg">{errors[checkoutForm.email.name].message}</p>}
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
                                            <input type="checkbox"/>
                                            <label
                                                className="ml-10 ">{t["I agree with the terms and conditions"]}</label>
                                            {errors[checkoutForm.isAgree.name] &&
                                                <p className="error-msg">{errors[checkoutForm.isAgree.name].message}</p>}
                                        </div>
                                        <div>
                                            <Suspense>
                                                <div className="agreement-info-wrap"
                                                     dangerouslySetInnerHTML={{__html: agreement}}>
                                                </div>
                                            </Suspense>
                                        </div>
                                        <button type="button"
                                                className="btn-hover">{t["Place your order"]}</button>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </div>
                </form>
            ) : (
                <div className="row">
                    <div className="col-lg-12">
                        <div className="item-empty-area text-center">
                            <div className="item-empty-area__icon mb-30">
                                <i className="pe-7s-cash"></i>
                            </div>
                            <div className="item-empty-area__text">
                                {t["No items found in checkout"]} <br/>{" "}
                                <Link href={"/"}>
                                    {t["Shop now"]}
                                </Link>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    </div>;
}

export const CheckoutCartDetails = ({cart, t}: {
    cart: Cart,
    t: { [key: string]: string }
}) => {
    return <div className="your-order-wrap gray-bg-4">
        <div className="your-order-product-info">
            <div className="your-order-top">
                <ul>
                    <li>{t["Product"]}</li>
                    <li>{t["Total"]}</li>
                </ul>
            </div>
            <div className="your-order-middle">
                <ul>
                    {cart.products.map((product, key) => {

                        return (
                            <li key={key}>
                                    <span className="order-middle-left" style={{width: 220}}>
                                      {product.description.name}
                                    </span>{" "}
                                <span>X {product.quantity}</span>
                                <span className="order-price">
                                      {
                                          product.finalPrice
                                      }
                                    </span>
                            </li>
                        );
                    })}
                </ul>
            </div>

        </div>

    </div>


}