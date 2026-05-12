'use client'
import {Product} from "@/types/product-groups";
import {Minus, Plus} from "lucide-react";
import React from 'react';
import {StoreContext} from "@/types/store-context";
import {useTranslations} from "next-intl";
import {Button} from "@/components/ui/button";
import {useProductDetailedAddToCart} from "@store-front/hooks/use-product-detailed-add-to-cart";
import {Badge} from "@/components/ui/badge";

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
        isInCart,
        maxQty,
        isOutOfStock,
        canDecrease,
        canIncrease
    } = useProductDetailedAddToCart(storeContext, product);


    return (
        <div className="mt-6">
            <div className="flex flex-col gap-4">
                <div
                    className="flex items-center justify-between rounded-xl border border-border bg-secondary/20 px-4 py-3">
                    <div className="min-w-0">
                        <p className="text-xs tracking-[0.22em] uppercase text-muted-foreground">
                            {t('QTY')}
                        </p>
                        <div className="mt-1 flex items-center gap-2">
                            <span className="text-base font-semibold text-foreground tabular-nums">
                                {quantity}
                            </span>
                            <Badge variant="secondary" className="rounded-full bg-background/85 text-foreground">
                                {maxQty} {t('AVAILABLE')}
                            </Badge>
                        </div>
                    </div>

                    <div className="flex items-center gap-2">
                        <Button
                            type="button"
                            variant="outline"
                            size="icon"
                            className="h-10 w-10 rounded-full"
                            onClick={decrementQuantity}
                            disabled={!canDecrease}
                            aria-label="Decrease quantity"
                        >
                            <Minus className="size-4"/>
                        </Button>

                        <Button
                            type="button"
                            variant="outline"
                            size="icon"
                            className="h-10 w-10 rounded-full"
                            onClick={incrementQuantity}
                            disabled={!canIncrease}
                            aria-label="Increase quantity"
                        >
                            <Plus className="size-4"/>
                        </Button>
                    </div>
                </div>

                <div className="grid gap-2">
                    <Button
                        type="button"
                        onClick={addToCart}
                        className="h-12 w-full rounded-xl"
                        size="lg"
                        disabled={isOutOfStock || isInCart}
                    >
                        {isOutOfStock ? t('OUT_OF_STOCK') : t('ADD_TO_CART')}
                    </Button>

                    <p className="text-xs text-muted-foreground">
                        {isInCart ? t('PRODUCT_ALREADY_EXIST_IN_CART') : t('ADD_TO_SECURE_CHECKOUT')}
                    </p>
                </div>
            </div>
        </div>
    );
};