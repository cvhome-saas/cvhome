'use server'

import {cookies} from 'next/headers'
import {RequestCookie} from "next/dist/compiled/@edge-runtime/cookies";
import {Cart} from "@/types/cart";
import {Store} from "@/types/store";
import {CartItems} from "@/componants/header/sub-components/CartItems";

export const NavCart = async ({store}: { store: Store }) => {
    const cookie: RequestCookie | undefined = cookies().get("store-ui-cart-id")
    var cart: Cart | undefined;
    if (cookie) {
        cart = await fetch(`http://localhost:8080/api/v1/cart/${cookie.value}?store=DEFAULT`)
            .then(it => it.json())
            .then(it => it as Cart)

    }
    return (
        <>
            <CartItems store={store} cart={cart}/>
        </>
    )
}