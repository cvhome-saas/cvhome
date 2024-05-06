import {Cart} from "@/types/cart";
import Cookies from "js-cookie";

export const updateLocalStorage = (cart: Cart | undefined) => {
    if (cart) {
        Cookies.set('store-ui-cart-id', cart.code);
        localStorage.setItem("store-ui-cart-data", JSON.stringify(cart))
        const elementById: HTMLElement | null = document.getElementById('shopbag-count');
        if (elementById) {
            elementById.innerHTML = cart.quantity + "";
            elementById.className = "count-style";
        }

    } else {
        Cookies.remove('store-ui-cart-id');
        localStorage.removeItem("store-ui-cart-data")
        const elementById: HTMLElement | null = document.getElementById('shopbag-count');
        if (elementById) {
            elementById.innerHTML = "";
            elementById.className = "";
        }
    }
}