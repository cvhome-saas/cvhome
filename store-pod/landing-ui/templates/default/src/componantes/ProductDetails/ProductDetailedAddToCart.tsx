'use client'
import {Product} from "@/types/product-groups";
import {Minus, Plus} from "lucide-react";
import React, {useCallback, useEffect, useRef, useState} from 'react';
import {StoreContext} from "@/types/store-context";
import {showToast} from "nextjs-toast-notify";
import {useTranslations} from "next-intl";
import {Button} from "@/components/ui/button";
import {getCartManager} from "@/services/cart-manager";
import {toastDirection} from "@/services/direction-utils";

export const ProductDetailedAddToCart = ({storeContext, product}: {
    storeContext: StoreContext,
    product: Product
}) => {
    const t = useTranslations('PAGE.PRODUCT');
    const [quantity, setQuantity] = useState(product.quantity > 0 ? 1 : 0);
    const inCartRef = useRef(false);
    const cartManager = getCartManager(storeContext);
    const updateCart = useCallback(async (newQty: number) => {
        cartManager.addProductToCart(product.sku, newQty, (cart) => {
            if (cart) {
                inCartRef.current = true;
                showToast.success(t('SUCCESSFULLY_ADDED_PRODUCT_TO_CART'), {
                    duration: 3000,
                    progress: false,
                    position: toastDirection(storeContext.locale),
                    transition: "bounceIn",
                    sound: false,
                });
            }
        }, (err) => {
            showToast.error(t('FAILED_TO_ADD_PRODUCT_TO_CART'), {
                duration: 3000,
                progress: false,
                position: toastDirection(storeContext.locale),
                transition: "bounceIn",
                sound: false,
            });
        });
    }, [cartManager, storeContext.locale, product.sku, t]);

    const syncWithCart = () => {
        const matchedProduct = cartManager.getMatchedProductsInCart(product.id);
        if (matchedProduct) {
            setQuantity(matchedProduct.quantity);
            inCartRef.current = true;
        } else {
            setQuantity(product.quantity > 0 ? 1 : 0);
            inCartRef.current = false;
        }
    };

    useEffect(cartManager.fullSubscribe(syncWithCart), [cartManager, product]);

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