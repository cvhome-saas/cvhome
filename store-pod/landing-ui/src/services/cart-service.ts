import {storeBaseServiceUrl, StoreContext} from "@/types/store-context";
import {Cart} from "@/types/cart";
import {CheckoutCart} from "@/types/checkout-cart";

export class CartService {
    public static getCart = (storeContext: StoreContext, cart: string): Promise<Cart> => {
        return fetch(`${storeBaseServiceUrl(storeContext)}/api/v1/cart/${cart}?store=${storeContext.store}`)
            .then(it => it.json() as unknown as Cart);
    }
    public static removeFromCart = (storeContext: StoreContext, cart: string, productSku: string) => {
        return fetch(`${storeBaseServiceUrl(storeContext)}/api/v1/cart/${cart}/product/${productSku}?store=${storeContext.store}`, {
            method: 'DELETE',
        });
    }
    public static addToCart = (storeContext: StoreContext, cartCode: string | undefined, product: string, quantity: number) => {
        if (cartCode) {
            return this.appendToCart(storeContext, cartCode, product, quantity);
        } else {
            return this.addToNewCart(storeContext, product, quantity);
        }
    }
    private static addToNewCart = (storeContext: StoreContext, product: string, quantity: number) => {
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

    private static appendToCart = (storeContext: StoreContext, cartCode: string, product: string, quantity: number) => {
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


    /*
    {
        "shippingQuote": ""
    ,
        "currency": "CAD"
    ,
        "payment": { "paymentType": "CREDITCARD", "transactionType": "CAPTURE", "paymentModule": "stripe", "amount": 200 }
    ,
        "customer": {
            "emailAddress": "tets@gg.cc",
            "billing": {
                "address": "ashraf",
                "city": "city",
                "postalCode": "ashraf",
                "country": "CA",
                "zone": "QC",
                "firstName": "ashraf",
                "lastName": "ashraf"
            }
        }
    }
    */

    public static checkout = (storeContext: StoreContext, code: string, checkoutCart: CheckoutCart) => {
        return fetch(`${storeBaseServiceUrl(storeContext)}/api/v1/cart/${code}/checkout?store=${storeContext.store}`, {
            method: 'POST',
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(checkoutCart)
        })
            .then(it => it.json())
            .then(it => {
                console.log(JSON.stringify(it))
            });
    }

}