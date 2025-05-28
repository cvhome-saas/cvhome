import {Cart} from "@/types/cart";
import {CheckoutCart} from "@/types/checkout-cart";
import {Order} from "@/types/order";
import {storeBaseServiceUrl, StoreContext} from "@/types/store-context";
import {handleResponse, post, put} from "@/utils/http-utils";
import {ReadableCountryList} from "@/types/country";

export class CartService {

    public static getCart = async (storeContext: StoreContext, cart: string): Promise<Cart | undefined> => {
        return fetch(`${storeBaseServiceUrl('order', storeContext)}/api/v1/cart/${cart}?store=${storeContext.store}&lang=${storeContext.locale}`)
            .then(it => handleResponse(it))
    }

    public static removeFromCart = async (storeContext: StoreContext, cart: string, productSku: string) => {
        return fetch(`${storeBaseServiceUrl('order', storeContext)}/api/v1/cart/${cart}/product/${productSku}?store=${storeContext.store}&lang=${storeContext.locale}`, {
            method: 'DELETE',
        });
    }

    public static removeFromCartThenGetCart = async (storeContext: StoreContext, cart: string, productSku: string) => {
        return CartService.removeFromCart(storeContext, cart, productSku).then((it) => CartService.getCart(storeContext, cart));
    }

    public static addToCart = async (storeContext: StoreContext, cartCode: string | undefined, product: string, quantity: number): Promise<Cart | undefined> => {
        const result: Promise<Response> = cartCode ? this.appendToCart(storeContext, cartCode, product, quantity) : this.addToNewCart(storeContext, product, quantity);
        return result
            .then(it => handleResponse(it))
    }

    private static addToNewCart = async (storeContext: StoreContext, product: string, quantity: number) => {
        return fetch(`${storeBaseServiceUrl('order', storeContext)}/api/v1/cart?store=${storeContext.store}&lang=${storeContext.locale}`, post({
            product,
            quantity
        }));
    }

    private static appendToCart = async (storeContext: StoreContext, cartCode: string, product: string, quantity: number) => {
        return fetch(`${storeBaseServiceUrl('order', storeContext)}/api/v1/cart/${cartCode}?store=${storeContext.store}&lang=${storeContext.locale}`, put({
            product,
            quantity
        }));

    }

    public static checkout = async (storeContext: StoreContext, code: string, checkoutCart: CheckoutCart): Promise<Order | undefined> => {
        return fetch(`${storeBaseServiceUrl('order', storeContext)}/api/v1/cart/${code}/checkout?store=${storeContext.store}&lang=${storeContext.locale}`, post(checkoutCart))
            .then(it => handleResponse(it))
    }

    public static getCountries = async (storeContext: StoreContext): Promise<ReadableCountryList | undefined> => {
        return fetch(`${storeBaseServiceUrl('order', storeContext)}/api/v1/country?store=${storeContext.store}&lang=${storeContext.locale}`)
            .then(it => handleResponse(it))
    }

}