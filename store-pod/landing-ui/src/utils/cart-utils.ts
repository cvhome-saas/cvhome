import {Cart} from "@/types/cart";
import {CART_DATA_KEY} from "@/types/constant";
import {Product} from "@/types/product-groups";

export function setCartData(cart: Cart | undefined) {
    if (cart && cart.code && typeof window !== "undefined") {
        localStorage.setItem(CART_DATA_KEY, JSON.stringify(cart))
    } else {
        localStorage.removeItem(CART_DATA_KEY);
    }
}

export function getCartData(): Cart | undefined {
    if (typeof window !== "undefined") {
        let cartStr: string | null = localStorage.getItem(CART_DATA_KEY);
        if (cartStr) {
            return JSON.parse(cartStr);
        } else {
            return undefined;
        }

    } else {
        return undefined;
    }
}

export const getMatchedProductsInCart = (product: Product) => {
    let currentCartData = getCartData();
    if (currentCartData) {
        return currentCartData.products.find(it => it.id === product.id);
    } else {
        return undefined;
    }
}

export const getCartCode = () => {
    const currentCartData = getCartData();
    return currentCartData ? currentCartData.code : undefined;
}
