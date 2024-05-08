'use client'
import React, {Fragment, useEffect, useState} from "react";
import {Link} from "@/navigation";
import {Cart, Product} from "@/types/cart";
import {StoreContext} from "@/types/store-context";
import {CartService} from "@/services/cart-service";
import Cookies from "js-cookie";
import {updateLocalStorage} from "@/services/utils";

export const FullCartBox = ({storeContext, t}: {
    storeContext: StoreContext,
    t: { [key: string]: string }
}) => {

    const [cart, setCart] = useState({} as Cart | undefined);
    const [loaded, setLoaded] = useState(false);

    const fetchCart = () => {
        const cartId = Cookies.get("store-ui-cart-id");
        if (cartId) {
            CartService.getCart(storeContext, cartId).then(it => {
                if (it && it.code) {
                    setCart(undefined);
                    setCart(it);
                    updateLocalStorage(it);
                }
                setLoaded(true);
            });
        }
    }

    useEffect(() => {
        fetchCart()
    }, []);


    const deleteAllFromCart = () => {
        setCart(undefined);
        updateLocalStorage(undefined);

    };
    const deleteFromCart = (p: Product) => {
        CartService.removeFromCart(storeContext, cart?.code || "", p.sku)
            .then(it => {
                fetchCart()
            });

    };

    const changeQty = (product: Product, qty: number) => {
        CartService.addToCart(storeContext, cart?.code, product.sku, qty)
            .then(it => {
                return it.json() as unknown as Cart;
            })
            .then(it => {
                if (it && it.code) {
                    setCart(undefined);
                    setCart(it);
                    updateLocalStorage(it);
                }
            });
    }

    const decreaseQuantity = (product: Product) => {
        if (product.quantity > 1) {
            changeQty(product, product.quantity - 1)
        }
    };

    const increaseQuantity = (product: Product) => {
        changeQty(product, product.quantity + 1)
    };

    return <div className="cart-main-area pt-90 pb-100">
        {
            loaded && <div className="container">
                {cart?.products && cart.products.length > 0 ? (
                    <>
                        <h3 className="cart-page-title">
                            {t["Your cart items"]}
                        </h3>
                        <div className="row custom-cart-item">
                            <div className="col-12">
                                <div className="table-content table-responsive cart-table-content">
                                    <table>
                                        <thead>
                                        <tr>
                                            <th>{t["Image"]}</th>
                                            <th>{t["Product Name"]}</th>
                                            <th>{t["Unit Price"]}</th>
                                            <th>{t["Qty"]}</th>
                                            <th>{t["Subtotal"]}</th>
                                            <th>{t["Action"]}</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {cart?.products && cart.products.map((product, key) => {
                                            const finalDiscountedPrice = product.finalPrice;
                                            return (
                                                <tr key={key}>
                                                    <td className="product-thumbnail">
                                                        <Link prefetch={false} href={"/product/" + product.description.friendlyUrl}>
                                                            <img className="img-fluid" src={product.image?.imageUrl}
                                                                 alt=""/>
                                                        </Link>
                                                    </td>

                                                    <td className="product-name">
                                                        <Link prefetch={false} href={"/product/" + product.description.friendlyUrl}>
                                                            {product.description.name}
                                                        </Link>
                                                    </td>

                                                    <td className="product-price-cart">
                                                        <span className="amount">
                                                            {finalDiscountedPrice}
                                                        </span>
                                                    </td>

                                                    <td className="product-quantity">
                                                        <div className="cart-plus-minus">
                                                            <button className="dec qtybutton"
                                                                    onClick={() => decreaseQuantity(product)}> -
                                                            </button>
                                                            <input className="cart-plus-minus-box" type="text"
                                                                   value={product.quantity} readOnly/>
                                                            <button className="inc qtybutton"
                                                                    onClick={() => increaseQuantity(product)}>+
                                                            </button>

                                                        </div>
                                                    </td>
                                                    <td className="product-subtotal">
                                                        {product.displaySubTotal}
                                                    </td>

                                                    <td className="product-remove">
                                                        <button onClick={() => deleteFromCart(product)}>
                                                            <i className="fa fa-times"></i>
                                                        </button>
                                                    </td>
                                                </tr>
                                            );
                                        })}
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                        <div className="row">
                            <div className="col-lg-12">
                                <div className="cart-shiping-update-wrapper">
                                    <div className="cart-clear">
                                        <button onClick={() => deleteAllFromCart()}>
                                            {t["Clear Shopping Cart"]}
                                        </button>
                                    </div>
                                    <div className="cart-shiping-update">
                                        <Link prefetch={false} href={"/"}>
                                            {t["Continue Shopping"]}
                                        </Link>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div className="row cart-custom-row">
                            <div className="col-lg-4 col-md-6">
                                &nbsp;
                            </div>
                            <div className="col-lg-8 col-md-6 cart-total">
                                <div className="box-custom">
                                    <div className="grand-totall cart-total-box">
                                        <div className="title-wrap">
                                            <h4 className="cart-bottom-title section-bg-gary-cart">
                                                {t["Cart Total"]}
                                            </h4>
                                        </div>
                                        <h5>
                                            {t["Total products"]}{" "}
                                            <span>
                                            {cart.displaySubTotal}
                                        </span>
                                        </h5>
                                        <h4 className="grand-totall-title">
                                            {t["Grand Total"]}{" "}
                                            <span>
                                            {cart.displaySubTotal}
                                        </span>
                                        </h4>
                                        <Link prefetch={false} href={"/checkout"}>
                                            {t["Proceed to Checkout"]}
                                        </Link>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </>
                ) : (
                    <div className="row">
                        <div className="col-lg-12">
                            <div className="item-empty-area text-center">
                                <div className="item-empty-area__icon mb-30">
                                    <i className="pe-7s-cart"></i>
                                </div>
                                <div className="item-empty-area__text">
                                    {t["No items found in cart"]} <br/>{" "}
                                    <Link prefetch={false} href={"/"}>
                                        {t["Shop now"]}
                                    </Link>
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        }


    </div>
}