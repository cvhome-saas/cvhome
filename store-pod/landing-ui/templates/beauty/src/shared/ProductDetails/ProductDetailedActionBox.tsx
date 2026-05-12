'use client'
import {Product} from "@/types/product-groups";
import {Minus, Plus} from "lucide-react";
import React from 'react';
import {StoreContext} from "@/types/store-context";
import {useTranslations} from "next-intl";
import {Button} from "@/components/ui/button";
import {useProductDetailedAddToCart} from "@store-front/hooks/use-product-detailed-add-to-cart";

export const ProductDetailedActionBox = ({storeContext, product}: {
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

    return <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-6">
        <div
            className="flex items-center justify-between sm:justify-start gap-6 p-2 bg-background/50 border border-primary/10 rounded-full shadow-inner">
            <Button variant="ghost" size="icon" className="rounded-full hover:bg-primary/10 text-primary h-10 w-10"
                    onClick={decrementQuantity} disabled={quantity <= 1}>
                <Minus className="size-5"/>
            </Button>
            <span className="w-8 text-center font-serif font-bold text-xl text-foreground">{quantity}</span>
            <Button variant="ghost" size="icon" className="rounded-full hover:bg-primary/10 text-primary h-10 w-10"
                    onClick={incrementQuantity} disabled={quantity >= product.quantity}>
                <Plus className="size-5"/>
            </Button>
        </div>
        <div className="flex-1">
            <Button type="button" onClick={addToCart}
                    className="w-full h-14 rounded-full bg-primary hover:bg-primary/90 text-primary-foreground shadow-xl shadow-primary/20 transition-all duration-300 active:scale-95 font-serif text-lg tracking-wide uppercase"
                    disabled={isInCart}>
                {t('ADD_TO_CART')}
            </Button>
        </div>
    </div>
};