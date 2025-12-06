'use client'
import {Product} from "@/types/product-groups";
import {Minus, Plus} from "lucide-react";
import React, {useCallback, useEffect, useRef, useState} from 'react';
import {getCartCode, getMatchedProductsInCart, setCartData} from "@/services/cart-utils";
import {CartService} from "@/services/cart-service";
import {emitter} from "next/client";
import {StoreContext} from "@/types/store-context";
import {showToast} from "nextjs-toast-notify";
import {useTranslations} from "next-intl";
import {Button} from "@/components/ui/button";

export const ProductDetailedAddToCart = ({storeContext, product}: {
    storeContext: StoreContext,
    product: Product
}) => {
    const t = useTranslations('PAGE.PRODUCT');
    const [quantity, setQuantity] = useState(product.quantity > 0 ? 1 : 0);
    const inCartRef = useRef(false);

    const updateCart = useCallback(async (newQty: number) => {
        try {
            const newCartData = await CartService.addToCart(storeContext, getCartCode(), product.sku, newQty);
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
                throw new Error("No cart data returned");
            }
        } catch (error) {
            console.error("Failed to update cart:", error);
            showToast.error(t('FAILED_TO_ADD_PRODUCT_TO_CART'), {
                duration: 3000,
                progress: false,
                position: "bottom-right",
                transition: "bounceIn",
                sound: false,
            });
        }
    }, [storeContext, product.sku, t]);

    useEffect(() => {
        const syncWithCart = () => {
            const matchedProduct = getMatchedProductsInCart(product);
            if (matchedProduct) {
                setQuantity(matchedProduct.quantity);
                inCartRef.current = true;
            } else {
                setQuantity(product.quantity > 0 ? 1 : 0);
                inCartRef.current = false;
            }
        };

        syncWithCart(); // Initial sync

        emitter.on("CART_STORAGE_CHANGED", syncWithCart);

        return () => {
            emitter.off("CART_STORAGE_CHANGED", syncWithCart);
        };
    }, [product]);

    const incrementQuantity = () => {
        const newQty = quantity + 1;
        if (product.quantity < newQty) return;

        setQuantity(newQty);
        if (inCartRef.current) {
            updateCart(newQty);
        }
    };

    const decrementQuantity = () => {
        const newQty = quantity - 1;
        if (newQty < 1) return;

        setQuantity(newQty);
        if (inCartRef.current) {
            updateCart(newQty);
        }
    };

    const addToCart = async () => {
        if (!inCartRef.current) {
            updateCart(quantity);
        }
    };

    return <div className="mt-10 flex items-center gap-4">
        <div className="flex items-center gap-2">
            <Button variant="outline" size="icon" onClick={decrementQuantity} disabled={quantity <= 1}>
                <Minus className="size-4"/>
            </Button>
            <span className="w-10 text-center font-semibold">{quantity}</span>
            <Button variant="outline" size="icon" onClick={incrementQuantity} disabled={quantity >= product.quantity}>
                <Plus className="size-4"/>
            </Button>
        </div>
        <div className="flex-1">
            <Button type="button" onClick={addToCart} className="w-full" size="lg" disabled={inCartRef.current}>
                {t('ADD_TO_CART')}
            </Button>
        </div>
    </div>
};