import {storeBaseServiceUrl, StoreContext} from "@/types/store-context";
import {Cart} from "@/types/cart";

export class CartService {
    public static getCart = async (storeContext: StoreContext, cart: string): Promise<Cart> => {
        return fetch(`${storeBaseServiceUrl(storeContext)}/api/v1/cart/${cart}?store=${storeContext.store}`)
            .then(it => it.json() as unknown as Cart);
    }
    public static removeFromCart = async (storeContext: StoreContext, cart: string, productId: number) => {
        return fetch(`${storeBaseServiceUrl(storeContext)}/api/v1/cart/${cart}/product/${productId}?store=${storeContext.store}`, {
            method: 'DELETE',
        });
    }
}