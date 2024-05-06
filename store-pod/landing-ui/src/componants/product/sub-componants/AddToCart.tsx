'use client'
import {Product} from "@/types/product-groups";
import {useState} from "react";
import {Cart} from "@/types/cart";
import {CartService} from "@/services/cart-service";
import {StoreContext} from "@/types/store-context";
import {updateLocalStorage} from "@/services/utils";


export const AddToCart = ({storeContext, addToCartType, product, t}: {
    storeContext: StoreContext,
    addToCartType: string,
    product: Product,
    t: { [key: string]: string }
}) => {
    const [qty, setQty] = useState(1)
    const changeQty = (type: boolean) => {

        if (type) {
            console.log("will up")
            setQty(qty + 1);
        } else {
            if (qty > 1) {
                console.log("will down")
                setQty(qty - 1);
            }
        }
    }

    const addToCart = () => {
        const item: string | null = localStorage.getItem("store-ui-cart-data");
        let cartCode: string | undefined;
        if (item) {
            cartCode = (JSON.parse(item) as unknown as Cart).code
        } else {
            cartCode = undefined;
        }


        return CartService.addToCart(storeContext, cartCode, product.description.friendlyUrl, 1)
            .then(it => {
                return it.json() as unknown as Cart
            })
            .then(it => {
                if (it && it.code) {
                    updateLocalStorage(it);
                }
                return it;
            });

    };

    return addToCartType == "FULL" ? <>
            <div className="pro-details-size-color"></div>
            <div className="pro-details-quality">
                <div className="cart-plus-minus">
                    <button className="dec qtybutton" onClick={() => changeQty(false)}>-</button>
                    <input className="cart-plus-minus-box" type="number" value={qty}
                           onChange={(event) => {
                           }}/>
                    <button className="inc qtybutton" onClick={() => changeQty(true)}>+</button>
                </div>
                <div className="pro-details-cart btn-hover">
                    <button onClick={addToCart}> {t['Add to cart']}</button>
                </div>
            </div>
        </> :
        <button className="active" title="Add to cart" onClick={addToCart}>
            <i className="fa fa-shopping-cart"></i>
        </button>
}