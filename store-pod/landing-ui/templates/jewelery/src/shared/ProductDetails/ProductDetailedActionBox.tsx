'use client'
import {Product} from "@/types/product-groups";
import {Minus, Plus} from "lucide-react";
import React from 'react';
import {StoreContext} from "@/types/store-context";
import {useTranslations} from "next-intl";
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

    return (
        <div className="flex flex-col gap-4">
            {/* Quantity selector */}
            <div className="flex items-center gap-0 border border-border w-fit">
                <button
                    onClick={decrementQuantity}
                    disabled={quantity <= 1}
                    className="flex items-center justify-center w-10 h-10 text-foreground hover:bg-accent transition-colors duration-200 disabled:opacity-30 disabled:cursor-not-allowed"
                    aria-label="Decrease quantity"
                >
                    <Minus className="size-3"/>
                </button>
                <span className="w-12 h-10 flex items-center justify-center text-sm font-medium text-foreground border-x border-border tracking-widest">
                    {quantity}
                </span>
                <button
                    onClick={incrementQuantity}
                    disabled={quantity >= product.quantity}
                    className="flex items-center justify-center w-10 h-10 text-foreground hover:bg-accent transition-colors duration-200 disabled:opacity-30 disabled:cursor-not-allowed"
                    aria-label="Increase quantity"
                >
                    <Plus className="size-3"/>
                </button>
            </div>

            {/* Add to cart button */}
            <button
                type="button"
                onClick={addToCart}
                disabled={isInCart}
                className="w-full py-3.5 px-8 text-xs tracking-[0.25em] uppercase font-medium transition-all duration-300 border border-foreground bg-foreground text-background hover:bg-primary hover:border-primary disabled:opacity-40 disabled:cursor-not-allowed"
            >
                {t('ADD_TO_CART')}
            </button>
        </div>
    );
};
