import {Cart} from "@store-front/types/cart";
import {CheckoutCart} from "@store-front/types/checkout-cart";
import {Order} from "@store-front/types/order";
import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {del, get, handleResponse, post, put} from "./http-utils";
import {PaymentType} from "@store-front/types/checkout-cart";
import {ReadableCountryList} from "@store-front/types/country";

export class CartService {

    public static getCart = async (storeContext: StoreContext, cart: string): Promise<Cart | undefined> => {
        return fetch(`${storeBaseServiceUrl('checkout', storeContext)}/api/v1/cart/${cart}?store=${storeContext.store}&lang=${storeContext.locale}`, get())
            .then(it => handleResponse<Cart>(it))
    }

    public static removeFromCart = async (storeContext: StoreContext, cart: string, productSku: string) => {
        return fetch(`${storeBaseServiceUrl('checkout', storeContext)}/api/v1/cart/${cart}/product/${productSku}?store=${storeContext.store}&lang=${storeContext.locale}`, del());
    }

    public static removeProduct = async (storeContext: StoreContext, cart: string, lineItemId: number) => {
        return fetch(`${storeBaseServiceUrl('checkout', storeContext)}/api/v1/cart/${cart}/product/${lineItemId}?store=${storeContext.store}&lang=${storeContext.locale}`, del())
            .then(() => CartService.getCart(storeContext, cart));
    }

    public static removeFromCartThenGetCart = async (storeContext: StoreContext, cart: string, productSku: string) => {
        return CartService.removeFromCart(storeContext, cart, productSku).then(() => CartService.getCart(storeContext, cart));
    }

    public static addToCart = async (storeContext: StoreContext, cartCode: string | undefined, product: string, quantity: number): Promise<Cart | undefined> => {
        const result: Promise<Response> = cartCode ? this.appendToCart(storeContext, cartCode, product, quantity) : this.addToNewCart(storeContext, product, quantity);
        return result
            .then(it => handleResponse<Cart>(it))
    }

    public static updateProductInCart = async (storeContext: StoreContext, cartCode: string, product: string, quantity: number): Promise<Cart | undefined> => {
        return fetch(`${storeBaseServiceUrl('checkout', storeContext)}/api/v1/cart/${cartCode}?store=${storeContext.store}&lang=${storeContext.locale}`, put({
            product,
            quantity
        })).then(it => handleResponse<Cart>(it));
    }

    public static checkout = async (storeContext: StoreContext, code: string, checkoutCart: CheckoutCart): Promise<Order | undefined> => {
        return fetch(`${storeBaseServiceUrl('checkout', storeContext)}/api/v1/cart/${code}/checkout?store=${storeContext.store}&lang=${storeContext.locale}`, post(checkoutCart))
            .then(it => handleResponse<Order>(it))
    }

    public static getCountries = async (storeContext: StoreContext): Promise<ReadableCountryList | undefined> => {
        return fetch(`${storeBaseServiceUrl('checkout', storeContext)}/api/v1/country?store=${storeContext.store}&lang=${storeContext.locale}`, get())
            .then(it => handleResponse<ReadableCountryList>(it))
    }

    public static getSupportedPaymentTypes = async (storeContext: StoreContext): Promise<PaymentType[] | undefined> => {
        return fetch(`${storeBaseServiceUrl('payment', storeContext)}/api/v1/public/payment-configuration/${storeContext.store}/supported-payment-types`, get())
            .then(it => handleResponse<PaymentType[]>(it))
    }

    private static addToNewCart = async (storeContext: StoreContext, product: string, quantity: number) => {
        return fetch(`${storeBaseServiceUrl('checkout', storeContext)}/api/v1/cart?store=${storeContext.store}&lang=${storeContext.locale}`, post({
            product,
            quantity
        }));
    }

    private static appendToCart = async (storeContext: StoreContext, cartCode: string, product: string, quantity: number) => {
        return fetch(`${storeBaseServiceUrl('checkout', storeContext)}/api/v1/cart/${cartCode}?store=${storeContext.store}&lang=${storeContext.locale}`, put({
            product,
            quantity
        }));

    }

}