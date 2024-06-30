'use client'
import React, {useState} from "react";
import {Cart} from "@/types/cart";
import {CheckoutForm} from "@/componants/checkout/CheckoutForm";
import {EmptyCheckoutCart} from "@/componants/checkout/EmptyCheckoutCart";
import {StoreContext} from "@/types/store-context";
import {CheckoutConfirmationModal} from "@/componants/checkout/sub-componants/CheckoutConfirmationModal";

export const CheckoutBox = ({storeContext, c, agreement, t}: {
    storeContext: StoreContext,
    c: Cart | undefined, agreement: string,
    t: { [key: string]: string }
}) => {
    const [cart, setCart] = useState(c);

    const [show, setShow] = useState(false);
    const [orderId, setOrderId] = useState(0);

    return <div className="checkout-area pt-95 pb-100">
        <CheckoutConfirmationModal show={show} setShow={setShow} orderId={orderId} t={t}/>

        <div className="container">
            {cart && cart.code && cart.products.length > 0 ?
                <CheckoutForm storeContext={storeContext} cart={cart} setCart={setCart} setShow={setShow}
                              setOrderId={setOrderId} t={t} agreement={agreement}/>
                : <EmptyCheckoutCart t={t}/>
            }
        </div>
    </div>;
}
