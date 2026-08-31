import {Cart, CART_DATA_KEY, CheckoutCart, Order, StoreContext} from "@store-front/types";
import {CartService} from "./cart-service";

export class CartManager {
    private static instance: CartManager;
    private cart: Cart | undefined = undefined;
    private storeContext: StoreContext;
    private subscribers: Map<string, CartCallbackSubscriberWrapper> = new Map();
    private nextSubscriptionId = 0;

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
        if (typeof window === "undefined") {
            return undefined;
        }
        try {
            const storedCart = localStorage.getItem(CART_DATA_KEY);
            if (storedCart) {
                return JSON.parse(storedCart);
            }
        } catch (error) {
            console.error("Failed to load cart from storage", error);
        }
        return undefined;
    }

    public subscribe(callback: CartSubscriber): string {
        const id = String(++this.nextSubscriptionId);
        const callbackWrapper: CartCallbackSubscriberWrapper = () => callback(this.cart);
        callbackWrapper();
        this.subscribers.set(id, callbackWrapper);
        return id;
    }

    public unsubscribe(id: string) {
        const subscriber: CartCallbackSubscriberWrapper | undefined = this.subscribers.get(id);
        if (subscriber) {
            this.subscribers.delete(id);
        }
    }

    public setCartData(cart: Cart | undefined) {
        if (typeof window !== "undefined") {
            if (cart && cart.code) {
                localStorage.setItem(CART_DATA_KEY, JSON.stringify(cart));
            } else {
                localStorage.removeItem(CART_DATA_KEY);
            }
        }
        this.cart = cart;
        this.subscribers.forEach(subscriber => subscriber());
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

    addProductToCart(productSku: string, newQty: number, onSuccess?: OnSuccessCallback<Cart | undefined>, onError?: OnErrorCallback) {
        CartService.addToCart(this.storeContext, this.cart?.code, productSku, newQty).then((cart: Cart | undefined) => {
            this.setCartData(cart);
            if (onSuccess) onSuccess(cart);
        }).catch((err: Error) => {
            if (onError) onError(err);
        });
    }


    /** Sets an absolute quantity for a line (the backend PUT is an upsert by sku). */
    updateQuantity(productSku: string, quantity: number, onSuccess?: OnSuccessCallback<Cart | undefined>, onError?: OnErrorCallback) {
        if (!this.cart) return;
        if (quantity < 1) {
            this.removeProduct(productSku, onSuccess, onError);
            return;
        }
        CartService.updateProductInCart(this.storeContext, this.cart.code, productSku, quantity).then((cart: Cart | undefined) => {
            this.setCartData(cart);
            if (onSuccess) onSuccess(cart);
        }).catch((err: Error) => {
            if (onError) onError(err);
        });
    }

    getCart(): Cart | undefined {
        return this.cart;
    }

    removeProduct(productSku: string, onSuccess?: OnSuccessCallback<Cart | undefined>, onError?: OnErrorCallback) {
        if (this.cart) {
            CartService.removeFromCartThenGetCart(this.storeContext, this.cart.code, productSku).then((cart: Cart | undefined) => {
                this.setCartData(cart);
                if (onSuccess) onSuccess(cart);
            }).catch((err: Error) => {
                if (onError) onError(err);
            });
        }
    }

    checkout(checkoutCart: CheckoutCart, onSuccess?: OnSuccessCallback<Order | undefined>, onError?: OnErrorCallback) {
        if (this.cart) {
            CartService.checkout(this.storeContext, this.cart.code, checkoutCart).then((order: Order | undefined) => {
                this.setCartData(undefined);
                if (onSuccess) onSuccess(order);
            }).catch((err: Error) => {
                if (onError) onError(err);
            });
        }
    }

}

export type CartSubscriber = (cart: Cart | undefined) => void;
export type CartCallbackSubscriberWrapper = () => void;
export type OnSuccessCallback<T> = (t: T) => void;
export type OnErrorCallback = (err: Error) => void;

export const getCartManager = (storeContext: StoreContext) => CartManager.getInstance(storeContext)
