import {Cart} from "@store-front/types/cart";
import {CheckoutCart} from "@store-front/types/checkout-cart";
import {Order} from "@store-front/types/order";
import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {apiFetch, del, get, post, put} from "./http-utils";
import {PaymentType} from "@store-front/types/checkout-cart";
import {ReadableCountryList} from "@store-front/types/country";

/**
 * Every call here is must-fail: this is the money path. A cart that silently fails to change, or a
 * checkout that resolves with `undefined`, is indistinguishable from success to the caller — which is
 * exactly how a cart got cleared for an order the backend had refused.
 */
export class CartService {

    public static getCart = async (storeContext: StoreContext, cart: string): Promise<Cart> => {
        return apiFetch<Cart>(
            `${storeBaseServiceUrl('checkout', storeContext)}/api/v1/cart/${cart}?store=${storeContext.store}&lang=${storeContext.locale}`,
            get());
    }

    /**
     * The response used to be returned unread, so a delete that failed looked exactly like one that
     * worked and the caller went on to refetch a cart still holding the item.
     */
    public static removeFromCart = async (storeContext: StoreContext, cart: string, productSku: string): Promise<void> => {
        await apiFetch<void>(
            `${storeBaseServiceUrl('checkout', storeContext)}/api/v1/cart/${cart}/product/${productSku}?store=${storeContext.store}&lang=${storeContext.locale}`,
            del());
    }

    public static removeProduct = async (storeContext: StoreContext, cart: string, lineItemId: number): Promise<Cart> => {
        await apiFetch<void>(
            `${storeBaseServiceUrl('checkout', storeContext)}/api/v1/cart/${cart}/product/${lineItemId}?store=${storeContext.store}&lang=${storeContext.locale}`,
            del());
        return CartService.getCart(storeContext, cart);
    }

    public static removeFromCartThenGetCart = async (storeContext: StoreContext, cart: string, productSku: string): Promise<Cart> => {
        await CartService.removeFromCart(storeContext, cart, productSku);
        return CartService.getCart(storeContext, cart);
    }

    public static addToCart = async (storeContext: StoreContext, cartCode: string | undefined, product: string, quantity: number): Promise<Cart> => {
        return cartCode
            ? this.appendToCart(storeContext, cartCode, product, quantity)
            : this.addToNewCart(storeContext, product, quantity);
    }

    public static updateProductInCart = async (storeContext: StoreContext, cartCode: string, product: string, quantity: number): Promise<Cart> => {
        return apiFetch<Cart>(
            `${storeBaseServiceUrl('checkout', storeContext)}/api/v1/cart/${cartCode}?store=${storeContext.store}&lang=${storeContext.locale}`,
            put({product, quantity}));
    }

    /**
     * The one call where the distinction between a refusal and no answer decides what the shopper is
     * told: a 422 `PAYMENT.INITIATE.REJECTED` is decided, a 502 `PAYMENT.INITIATE.FAILED` is not.
     */
    public static checkout = async (storeContext: StoreContext, code: string, checkoutCart: CheckoutCart): Promise<Order> => {
        return apiFetch<Order>(
            `${storeBaseServiceUrl('checkout', storeContext)}/api/v1/cart/${code}/checkout?store=${storeContext.store}&lang=${storeContext.locale}`,
            post(checkoutCart, {auth: true}));
    }

    /** Must fail: an empty country list makes the address form unusable without saying why. */
    public static getCountries = async (storeContext: StoreContext): Promise<ReadableCountryList> => {
        return apiFetch<ReadableCountryList>(
            `${storeBaseServiceUrl('checkout', storeContext)}/api/v1/country?store=${storeContext.store}&lang=${storeContext.locale}`,
            get());
    }

    /** Must fail: no payment types renders as "this store takes no payments", which may not be true. */
    public static getSupportedPaymentTypes = async (storeContext: StoreContext): Promise<PaymentType[]> => {
        return apiFetch<PaymentType[]>(
            `${storeBaseServiceUrl('payment', storeContext)}/api/v1/public/payment-configuration/${storeContext.store}/supported-payment-types`,
            get());
    }

    private static addToNewCart = async (storeContext: StoreContext, product: string, quantity: number): Promise<Cart> => {
        return apiFetch<Cart>(
            `${storeBaseServiceUrl('checkout', storeContext)}/api/v1/cart?store=${storeContext.store}&lang=${storeContext.locale}`,
            post({product, quantity}));
    }

    private static appendToCart = async (storeContext: StoreContext, cartCode: string, product: string, quantity: number): Promise<Cart> => {
        return apiFetch<Cart>(
            `${storeBaseServiceUrl('checkout', storeContext)}/api/v1/cart/${cartCode}?store=${storeContext.store}&lang=${storeContext.locale}`,
            put({product, quantity}));
    }

}
