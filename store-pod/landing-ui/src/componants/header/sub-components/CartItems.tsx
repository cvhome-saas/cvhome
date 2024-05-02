'use client'
import {Store} from "@/types/store";
import {Cart, Product} from "@/types/cart";
import {Fragment, useState} from "react";
import {StoreContext} from "@/types/store-context";
import {Link} from "@/navigation";
import {CartService} from "@/services/cart-service";
import Cookies from "js-cookie";

export const CartItems = ({storeContext, store, cart, t}: {
    storeContext: StoreContext,
    store: Store,
    cart: Cart | undefined,
    t: { [key: string]: string }
}) => {
    if (cart && typeof window !== "undefined") {
        localStorage.setItem("store-ui-cart-data", JSON.stringify(cart))
    }
    const [cartCode, setCartCode] = useState(cart?.code)
    const [displayTotal, setDisplayTotal] = useState(cart?.displayTotal)
    const [quantity, setQuantity] = useState(cart?.quantity)
    const [products, setProducts] = useState(cart?.products || [])


    const deleteFromCart = async (p: Product) => {
        await CartService.removeFromCart(storeContext, cartCode || "", p.sku);
        if (typeof window !== "undefined") {
            if (products.length == 1 || products.length == 0) {
                localStorage.removeItem("store-ui-cart-data");
                Cookies.remove('store-ui-cart-id')
                setCartCode(undefined);
                setDisplayTotal(undefined);
                setQuantity(undefined);
            } else if (products.length > 1) {
                const newCart = await CartService.getCart(storeContext, cartCode || "");
                setProducts(newCart.products);
                setDisplayTotal(newCart.displayTotal)
                setQuantity(newCart.quantity)
            }
        }
    };

    const [active, setActive] = useState('shopping-cart-content');
    const showOrHideCart = () => {
        if (active == 'shopping-cart-content') {
            setActive('shopping-cart-content active');
        } else {
            setActive('shopping-cart-content');
        }
    };
    return <div>
        <div className="same-style cart-wrap d-none d-lg-block">
            <button className="icon-cart" onClick={showOrHideCart}>
                <i className="pe-7s-shopbag"></i>
                <span className={quantity && quantity > 0 ? "count-style" : ""}>{quantity}</span>
            </button>
            <div className={active}>
                {products.length > 0 ? (
                    <Fragment>
                        <ul>
                            {products.map((single, key) => {
                                // const finalProductPrice = single.originalPrice;
                                const finalDiscountedPrice = single.finalPrice;
                                // cartTotalPrice += single.price;
                                return (
                                    <li className="single-shopping-cart" key={key}>
                                        <div className="shopping-cart-img">
                                            <Link href={"/product/" + single.id}>
                                                <img alt="" src={defaultImage(single)} className="img-fluid"/>
                                            </Link>
                                        </div>
                                        <div className="shopping-cart-title">
                                            <h4>
                                                <Link href={"/"}>
                                                    {single.description.name}
                                                </Link>
                                            </h4>
                                            <h6>{t['Qty']}: {single.quantity}</h6>
                                            <span>
                          {finalDiscountedPrice}
                        </span>
                                            {/* {single.selectedProductColor &&
                          single.selectedProductSize ? (
                            <div className="cart-item-variation">
                              <span>Color: {single.selectedProductColor}</span>
                              <span>Size: {single.selectedProductSize}</span>
                            </div>
                          ) : (
                            ""
                          )} */}
                                        </div>
                                        <div className="shopping-cart-delete">
                                            <button
                                                onClick={() => deleteFromCart(single)}
                                            >
                                                <i className="fa fa-times-circle"/>
                                            </button>
                                        </div>
                                    </li>
                                );
                            })}
                        </ul>
                        <div className="shopping-cart-total">
                            <h4>
                                {t['Total']} :
                                <span className="shop-total">
                                        {displayTotal}
                                    </span>
                            </h4>
                        </div>
                        <div className="shopping-cart-btn btn-hover text-center">
                            <Link className="default-btn" href={"/cart"}>
                                {t['View Cart']}
                            </Link>
                            <Link className="default-btn" href={"/checkout"}>
                                {t['Checkout']}
                            </Link>
                        </div>
                    </Fragment>
                ) : (
                    <p className="text-center">{t['No items added to cart']}</p>
                )}
            </div>
        </div>
    </div>
};

function defaultImage(product: Product): string {
    if (product.images && product.images.length > 0) {
        return product.images[0].imageUrl;
    } else if (product.image != null) {
        return product.image.imageUrl;
    } else {
        return '';
    }
}
