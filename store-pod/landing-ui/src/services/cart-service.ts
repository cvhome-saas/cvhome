import {storeBaseServiceUrl, StoreContext} from "@/types/store-context";
import {Cart} from "@/types/cart";

export class CartService {
    public static getCart = async (storeContext: StoreContext, cart: string): Promise<Cart> => {
        return fetch(`${storeBaseServiceUrl(storeContext)}/api/v1/cart/${cart}?store=${storeContext.store}`)
            .then(it => it.json() as unknown as Cart);
    }
    public static removeFromCart = async (storeContext: StoreContext, cart: string, productSku: string) => {
        return fetch(`${storeBaseServiceUrl(storeContext)}/api/v1/cart/${cart}/product/${productSku}?store=${storeContext.store}`, {
            method: 'DELETE',
        });
    }
    public static addToCart = async (storeContext: StoreContext, cartCode: string | undefined, product: string, quantity: number) => {
        if (cartCode) {
            return this.appendToCart(storeContext, cartCode, product, quantity);
        } else {
            return this.addToNewCart(storeContext, product, quantity);
        }
    }
    private static addToNewCart = async (storeContext: StoreContext, product: string, quantity: number) => {
        return fetch(`${storeBaseServiceUrl(storeContext)}/api/v1/cart?store=${storeContext.store}`, {
            method: 'POST',
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                product,
                quantity
            })
        });
    }

    private static appendToCart = async (storeContext: StoreContext, cartCode: string, product: string, quantity: number) => {
        return fetch(`${storeBaseServiceUrl(storeContext)}/api/v1/cart/${cartCode}?store=${storeContext.store}`, {
            method: 'PUT',
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                product,
                quantity
            })
        });

    }
}