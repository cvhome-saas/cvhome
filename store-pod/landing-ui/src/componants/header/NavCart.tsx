'use server'

import {cookies} from 'next/headers'
import {RequestCookie} from "next/dist/compiled/@edge-runtime/cookies";
import {Cart} from "@/types/cart";
import {Store} from "@/types/store";
import {CartItems} from "@/componants/header/sub-components/CartItems";
import {getTranslations} from "next-intl/server";
import {baseServiceUrl, StoreContext} from "@/types/store-context";

export const NavCart = async ({storeContext,store}: { storeContext:StoreContext,store: Store }) => {
    const cookie: RequestCookie | undefined = cookies().get("store-ui-cart-id" as any)
    var cart: Cart | undefined;
    if (cookie) {
        cart = await fetch(`${baseServiceUrl(storeContext,'store')}/api/v1/cart/${cookie.value}?store=${store.code}`)
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
            <CartItems storeContext={storeContext} store={store} cart={cart} t={x}/>
        </>
    )
}