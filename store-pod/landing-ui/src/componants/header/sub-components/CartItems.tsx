'use client'
import {Store} from "@/types/store";
import {Cart, Product} from "@/types/cart";
import {Fragment, useEffect, useState} from "react";
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
    let [loaded, setLoaded] = useState(false);
    let currentCart: Cart | undefined = cart;
    const [cartCode, setCartCode] = useState(currentCart?.code)
    const [displayTotal, setDisplayTotal] = useState(currentCart?.displayTotal)
    const [quantity, setQuantity] = useState(currentCart?.quantity)
    const [products, setProducts] = useState(currentCart?.products || [])

    const addRemoveCartData = (newCart: Cart | undefined) => {
        if (newCart && newCart.code && newCart.products && newCart.products.length > 0 && typeof window !== "undefined") {
            Cookies.set('store-ui-cart-id', newCart.code);
            localStorage.setItem("store-ui-cart-data", JSON.stringify(newCart));

        } else {
            Cookies.remove('store-ui-cart-id');
            localStorage.removeItem("store-ui-cart-data");
        }

    }


    const updateCartItems = (cart: Cart | undefined) => {
        if (cart && cart.code) {
            setCartCode(cart.code);
            setProducts(cart.products);
            setDisplayTotal(cart.displayTotal)
            setQuantity(cart.quantity)
        } else {
            setCartCode(undefined);
            setProducts([]);
            setDisplayTotal(undefined);
            setQuantity(undefined);
        }
    }


    const refreshCartView = (newCart: Cart | undefined) => {
        currentCart = cart;
        addRemoveCartData(newCart);
        updateCartItems(newCart);
    };

    const deleteFromCart = async (p: Product) => {
        await CartService.removeFromCart(storeContext, cartCode || "", p.sku);
        products.length <= 1 ? refreshCartView(undefined) : refreshCartView(await CartService.getCart(storeContext, cartCode || ""))
    };

    const [active, setActive] = useState('shopping-cart-content');
    const showOrHideCart = () => {
        if (active == 'shopping-cart-content') {
            const itemStr: string | null = localStorage.getItem("store-ui-cart-data");
            if (itemStr) {
                try {
                    updateCartItems(JSON.parse(itemStr) as Cart | undefined)
                } catch (e) {
                }
            }

            setActive('shopping-cart-content active');
        } else {
            setActive('shopping-cart-content');
        }
    };

    useEffect(() => {
        if (!loaded) {
            setLoaded(true);
            addRemoveCartData(currentCart);
        }
    })

    return <div>
        <div className="same-style cart-wrap d-none d-lg-block">
            <button className="icon-cart" onClick={showOrHideCart}>
                <i className="pe-7s-shopbag"></i>
                <span className={quantity && quantity > 0 ? "count-style" : ""} id={"shopbag-count"}>{quantity}</span>
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
    } else if (product.image != undefined) {
        return product.image.imageUrl;
    } else {
        return '';
    }
}
