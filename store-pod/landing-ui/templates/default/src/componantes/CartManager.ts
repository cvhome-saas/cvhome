import {emitter} from "next/client";
import {Cart} from "@/types/cart";
import {StoreContext} from "@/types/store-context";
import {CART_DATA_KEY} from "@/types/constant";
import {v4 as uuidv4} from 'uuid';


const CART_CHANGED_EVENT = "CART_STORAGE_CHANGED";

class CartManager {
    private static instance: CartManager;
    private cart: Cart | undefined = undefined;
    private storeContext: StoreContext | undefined = undefined;
    private subscribers: Map<string, CartCallbackSubscriberWrapper> = new Map();

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

    public subscribe(callback: CartSubscriber): string {
        const id = uuidv4();
        const callbackWrapper: CartCallbackSubscriberWrapper = () => callback(this.cart);
        callbackWrapper();
        this.subscribers.set(id, callbackWrapper);
        emitter.on(CART_CHANGED_EVENT, callbackWrapper);
        return id;
    }

    public unsubscribe(id: string) {
        const subscriber: CartCallbackSubscriberWrapper | undefined = this.subscribers.get(id);
        if (subscriber) {
            emitter.off(CART_CHANGED_EVENT, subscriber);
            this.subscribers.delete(id);
        }
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

    fullSubscribe(setCart: CartSubscriber) {
        return () => {
            const subscriptionId = this.subscribe(setCart);
            return () => this.unsubscribe(subscriptionId);
        };
    }

}

export type CartSubscriber = (cart: Cart | undefined) => void;
export type CartCallbackSubscriberWrapper = () => void;

export const getCartManager = (storeContext: StoreContext) => CartManager.getInstance(storeContext)
