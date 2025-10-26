'use client'
import {Product} from "@/types/product-groups";
import {MinusIcon, PlusIcon} from "@heroicons/react/16/solid";
import React, {useEffect, useRef, useState} from 'react';
import {getCartCode, getMatchedProductsInCart, setCartData} from "@/services/cart-utils";
import {CartService} from "@/services/cart-service";
import {emitter} from "next/client";
import {StoreContext} from "@/types/store-context";
import {showToast} from "nextjs-toast-notify";
import {useTranslations} from "next-intl";

export const ProductDetailedAddToCart = ({storeContext, product}: {
    storeContext: StoreContext,
    product: Product
}) => {
    const t = useTranslations('PAGE.PRODUCT');
    const [quantity, setQuantity] = useState(1);
    const inCartRef = useRef(false);

    const updateCart = async (newQty: number) => {
        return await CartService.addToCart(storeContext, getCartCode(), product.sku, newQty)
            .then(newCartData => {
                if (newCartData) {
                    inCartRef.current = true;
                    setCartData(newCartData);
                    emitter.emit("CART_STORAGE_CHANGED", "");
                    showToast.success(t('SUCCESSFULLY_ADDED_PRODUCT_TO_CART'), {
                        duration: 3000,
                        progress: false,
                        position: "bottom-right",
                        transition: "bounceIn",
                        sound: false,
                    });
                } else {
                    showToast.error(t('FAILED_TO_ADD_PRODUCT_TO_CART'), {
                        duration: 3000,
                        progress: false,
                        position: "bottom-right",
                        transition: "bounceIn",
                        sound: false,
                    });
                }
            });
    };


    const copyCartMatchedProducts = (cartMatchedCartProduct: Product) => {
        setQuantity(cartMatchedCartProduct.quantity)
        inCartRef.current = true;
    }
    const setDefaultQuantity = () => {
        if (product.quantity == 0) {
            setQuantity(0);
        } else {
            setQuantity(1);
        }
        inCartRef.current = false;
    }


    useEffect(() => {
        let matchedProducts = getMatchedProductsInCart(product);
        if (matchedProducts) {
            copyCartMatchedProducts(matchedProducts);
        } else {
            setDefaultQuantity()
        }
        emitter.on("CART_STORAGE_CHANGED", data => {
            let matchedProducts = getMatchedProductsInCart(product);
            if (matchedProducts == undefined) {
                setDefaultQuantity();
            }
        });
    }, []);

    const changeQuantity = (fn: (x: number) => number): number => {
        const newQty = fn(quantity);
        setQuantity(newQty);
        return newQty;
    };

    const incrementQuantity = () => {
        const newQty = changeQuantity(prevQuantity => {
            if (product.quantity > prevQuantity) {
                return prevQuantity + 1;
            } else {
                return prevQuantity;
            }
        });
        if (inCartRef.current) {
            updateCart(newQty).then()
        }
    };

    const decrementQuantity = () => {
        const newQty = changeQuantity(prevQuantity => {
            return prevQuantity > 1 ? prevQuantity - 1 : prevQuantity;
        });
        if (inCartRef.current) {
            updateCart(newQty).then()
        }
    };

    const addToCart = async () => {
        if (!inCartRef.current) {
            updateCart(quantity).then();
        }
    };

    return <div className="mt-10">
        <div className="flex items-center mt-4 w-full">
            <button
                type="button"
                onClick={decrementQuantity}
                className="p-2 rounded-full me-2 hover:bg-neutral disabled:opacity-50"
            >
                <MinusIcon className="size-5 text-neutral"/>
            </button>
            <span className="mx-2">{quantity}</span>
            <button
                type="button"
                onClick={incrementQuantity}
                className="p-2 rounded-full me-4 hover:bg-neutral"
            >
                <PlusIcon className="size-5 text-neutral"/>
            </button>
            <button
                type="button"
                onClick={addToCart}
                className="bg-primary text-foreground py-1 px-4 rounded"
            >
                {t('ADD_TO_CART')}
            </button>
        </div>
    </div>
};