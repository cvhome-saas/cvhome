'use server'

import {cookies} from 'next/headers'
import {RequestCookie} from "next/dist/compiled/@edge-runtime/cookies";
import {Cart} from "@/types/cart";
import {Store} from "@/types/store";
import {CartItems} from "@/componants/header/sub-components/CartItems";
import {getTranslations} from "next-intl/server";

export const NavCart = async ({store}: { store: Store }) => {
    const cookie: RequestCookie | undefined = cookies().get("store-ui-cart-id")
    var cart: Cart | undefined;
    if (cookie) {
        cart = await fetch(`http://localhost:8080/api/v1/cart/${cookie.value}?store=${store.code}`)
            .then(it => it.json())
            .then(it => it as Cart)

    }
    const t = await getTranslations('Cart');
    const x = {
        'No items added to cart': t('No items added to cart'),
        'Checkout': t('Checkout'),
        'View Cart': t('View Cart'),
        'Total': t('Total'),
        'Qty': t('Qty'),
    }
    return (
        <>
            <CartItems store={store} cart={cart} t={x}/>
        </>
    )
}