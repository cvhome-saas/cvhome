'use client'
import {Store} from "@/types/store";
import {Cart} from "@/types/cart";
import {Fragment} from "react";
import Link from "next/link";

export const CartItems = ({store, cart}: { store: Store, cart: Cart | undefined }) => {
    if (cart && typeof window !== "undefined") {
        localStorage.setItem("store-ui-cart-data", JSON.stringify(cart))
    }
    return (
        <>
            <div className="shopping-cart-content active">
                {cart && cart.products.length > 0 ? (
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
                                            <h6>Qty: {single.quantity}</h6>
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
                                                // onClick={() => deleteFromCart(cartData.code, single, defaultStore, addToast)}
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
                                Total :
                                <span className="shop-total">
                {cart.displayTotal}
              </span>
                            </h4>
                        </div>
                        <div className="shopping-cart-btn btn-hover text-center">
                            <Link className="default-btn" href={"/cart"}>
                                View Cart
                            </Link>
                            <Link className="default-btn" href={"/checkout"}>
                                Checkout
                            </Link>
                        </div>
                    </Fragment>
                ) : (
                    <p className="text-center">No items added to cart</p>
                )}
            </div>

        </>
    )
};

function defaultImage(product) {
    if(product.images && product.images.length > 0) {
        return product.images[0].imageUrl;
    } else if(product.image != null) {
        return product.imageUrl;
    } else {
        return null;
    }
}
