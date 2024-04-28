'use client'
import {Store} from "@/types/store";
import {Cart, Product} from "@/types/cart";
import {Fragment, useState} from "react";
import Link from "next/link";

export const CartItems = ({store, cart, t}: { store: Store, cart: Cart | undefined, t: {} }) => {
    if (cart && typeof window !== "undefined") {
        localStorage.setItem("store-ui-cart-data", JSON.stringify(cart))
    }

    const [active, setActive] = useState('shopping-cart-content');
    const deleteFromCart = async (p: Product) => {
        await fetch(`http://localhost:8080/api/v1/cart/${cart?.code}/product/${p.id}?store=${store.code}`, {
            method: 'DELETE',
        });
    };
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
                <span className="count-style">{cart?.quantity}</span>
            </button>
            <div className={active}>
                {cart && cart.products && cart.products.length > 0 ? (
                    <Fragment>
                        <ul>
                            {cart.products.map((single, key) => {
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
                                        {cart.displayTotal}
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

function defaultImage(product) {
    if (product.images && product.images.length > 0) {
        return product.images[0].imageUrl;
    } else if (product.image != null) {
        return product.imageUrl;
    } else {
        return null;
    }
}
