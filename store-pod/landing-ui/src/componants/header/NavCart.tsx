'use server'

import {cookies} from 'next/headers'
import {RequestCookie} from "next/dist/compiled/@edge-runtime/cookies";
import {Cart} from "@/types/cart";
import {Store} from "@/types/store";
import {CartItems} from "@/componants/header/sub-components/CartItems";
import {getTranslations} from "next-intl/server";
import {StoreContext} from "@/types/store-context";
import {CartService} from "@/services/cart-service";

export const NavCart = async ({storeContext, store}: { storeContext: StoreContext, store: Store }) => {
    const cookie: RequestCookie | undefined = cookies().get("store-ui-cart-id" as any)
    let cart: Cart | undefined;
    if (cookie) {
        cart = await CartService.getCart(storeContext, cookie.value);
    }
    const t = await getTranslations('Cart');
    return (
        <>
            <CartItems storeContext={storeContext} store={store} cart={cart} t={{
                'No items added to cart': t('No items added to cart'),
                'Checkout': t('Checkout'),
                'View Cart': t('View Cart'),
                'Total': t('Total'),
                'Qty': t('Qty'),
            }}/>
        </>
    )
}