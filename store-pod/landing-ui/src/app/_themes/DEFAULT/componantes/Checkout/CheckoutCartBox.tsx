'use client'
import {useEffect, useState} from "react";
import {Cart} from "@/types/cart";
import {getCartData} from "@/utils/cart-utils";
import {CartProductList} from "@/app/_themes/DEFAULT/componantes/Cart/CartProductList";
import {StoreContext} from "@/types/store-context";
import {CartSummary} from "@/app/_themes/DEFAULT/componantes/Cart/CartSummary";
import {emitter} from "next/client";
import {useTranslations} from "next-intl";

export const CheckoutCartBox = ({storeContext}: { storeContext: StoreContext }) => {
    const t = useTranslations('PAGE.CHECKOUT');
    const [cart, setCart] = useState<Cart | undefined>()

    useEffect(() => {
        setCart(getCartData());
        emitter.on("CART_STORAGE_CHANGED", data => {
            setCart(getCartData())
        });
    }, []);

    return (
        <>
            <h2 className="text-xl font-bold mb-4 text-foreground">{t('CART_DETAILS')}</h2>
            <div className="max-h-96 overflow-y-auto">
                <CartProductList storeContext={storeContext} cart={cart} setCart={setCart}/>
            </div>
            <div className="mt-6">
                <hr className="my-4 border-border"/>
                <CartSummary cart={cart}/>
            </div>
        </>
    )
}