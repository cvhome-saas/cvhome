import {emitter} from "next/client";
import {Cart} from "@/types/cart";
import {StoreContext} from "@/types/store-context";
import {CART_DATA_KEY} from "@/types/constant";


const CART_CHANGED_EVENT = "CART_STORAGE_CHANGED";

class CartManager {
    private static instance: CartManager;
    private cart: Cart | undefined = undefined;
    private storeContext: StoreContext | undefined = undefined;

    private constructor(storeContext: StoreContext) {
        this.storeContext = storeContext;
        this.setCartData(CartManager.loadFromStorage());
    }

    public static getInstance(storeContext: StoreContext): CartManager {
        if (!CartManager.instance) {
            CartManager.instance = new CartManager(storeContext);
        }
        return CartManager.instance;
    }

    private static loadFromStorage(): Cart | undefined {
        let cart: Cart | undefined = undefined;
        try {
            const storedCart = localStorage.getItem(CART_DATA_KEY);
            if (storedCart) {
                cart = JSON.parse(storedCart);
            }
        } catch (error) {
            console.error("Failed to load cart from storage", error);
            cart = undefined;
        }
        return cart;
    }

    public subscribe(callback: () => void) {
        callback();
        emitter.on(CART_CHANGED_EVENT, callback);
    }

    public unsubscribe(callback: () => void) {
        emitter.off(CART_CHANGED_EVENT, callback);
    }

    public consumeCart(callback: (cart: Cart | undefined) => void) {
        if (this.cart) callback(this.cart)
    }

    public setCartData(cart: Cart | undefined) {
        if (cart && cart.code && typeof window !== "undefined") {
            localStorage.setItem(CART_DATA_KEY, JSON.stringify(cart))
        } else {
            localStorage.removeItem(CART_DATA_KEY);
        }
        this.cart = cart;
        emitter.emit(CART_CHANGED_EVENT, "");
    }

    getCartCode() {
        return this.cart?.code;
    }

    getMatchedProductsInCart(productId: number) {
        if (this.cart && this.cart.products) {
            return this.cart.products.find(it => it.id === productId);
        } else {
            return undefined;
        }

    }
}

export const getCartManager = (storeContext: StoreContext) => CartManager.getInstance(storeContext)
