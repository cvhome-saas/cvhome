import {Cart} from "@/types/cart";
import React from "react";

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