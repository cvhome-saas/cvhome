import React, {Suspense} from "react";
import {Cart} from "@/types/cart";
import {CheckoutForm} from "@/componants/checkout/CheckoutForm";
import {EmptyCheckoutCart} from "@/componants/checkout/EmptyCheckoutCart";
import {StoreContext} from "@/types/store-context";

export const CheckoutBox = ({storeContext,cart, agreement, t}: {
    storeContext:StoreContext,
    cart: Cart | undefined, agreement: string,
    t: { [key: string]: string }
}) => {

    return <div className="checkout-area pt-95 pb-100">
        <div className="container">
            {cart && cart.code && cart.products.length > 0 ?
                <CheckoutForm storeContext={storeContext} cart={cart} t={t} agreement={agreement}/>
                : <EmptyCheckoutCart t={t}/>
            }
        </div>
    </div>;
}
