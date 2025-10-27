import {Cart} from "@store-front/types";
import {CART_DATA_KEY} from "@store-front/types";
import {Product} from "@store-front/types";

export function setCartData(cart: Cart | undefined) {
    if (cart && cart.code && typeof window !== "undefined") {
        localStorage.setItem(CART_DATA_KEY, JSON.stringify(cart))
    } else {
        localStorage.removeItem(CART_DATA_KEY);
    }
}

export function getCartData(): Cart | undefined {
    if (typeof window !== "undefined") {
        const cartStr: string | null = localStorage.getItem(CART_DATA_KEY);
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
    const currentCartData = getCartData();
    if (currentCartData && currentCartData.products) {
        return currentCartData.products.find(it => it.id === product.id);
    } else {
        return undefined;
    }
}

export const getCartCode = () => {
    const currentCartData = getCartData();
    return currentCartData ? currentCartData.code : undefined;
}
