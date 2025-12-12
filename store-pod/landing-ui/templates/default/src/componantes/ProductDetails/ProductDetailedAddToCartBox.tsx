'use client'
import {Product} from "@/types/product-groups";
import {Minus, Plus} from "lucide-react";
import React from 'react';
import {StoreContext} from "@/types/store-context";
import {useTranslations} from "next-intl";
import {Button} from "@/components/ui/button";
import {useProductDetailedAddToCart} from "@/hooks/use-product-detailed-add-to-cart";

export const ProductDetailedAddToCartBox = ({storeContext, product}: {
    storeContext: StoreContext,
    product: Product
}) => {
    const t = useTranslations('PAGE.PRODUCT');
    const {
        quantity,
        incrementQuantity,
        decrementQuantity,
        addToCart,
        isInCart
    } = useProductDetailedAddToCart(storeContext, product);

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
            <Button type="button" onClick={addToCart} className="w-full" size="lg" disabled={isInCart}>
                {t('ADD_TO_CART')}
            </Button>
        </div>
    </div>
};