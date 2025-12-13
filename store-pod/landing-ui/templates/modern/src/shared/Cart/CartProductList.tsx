'use client'
import React, {useMemo} from "react";
import Image from 'next/image';
import {useTranslations} from "next-intl";

import {Link} from "@/i18n/navigation";
import {Button} from "@/components/ui/button";
import {cn} from "@/lib/utils";

import {Cart} from "@/types/cart";
import {StoreContext} from "@/types/store-context";
import {Product} from "@store-front/types";
import {useCart} from "@store-front/hooks/use-cart";

export const CartProductList = ({storeContext, cart}: {
    storeContext: StoreContext,
    cart: Cart | undefined,
}) => {
    const {removeProduct} = useCart(storeContext);
    const t = useTranslations('COMPONENTS.CART');

    if (!cart || !cart.products || cart.products.length === 0) {
        return (
            <div className="rounded-2xl border border-border bg-background p-6 text-center">
                <p className="text-sm text-muted-foreground">{t('CART_IS_EMPTY')}</p>
            </div>
        );
    }

    return (
        <div className="flow-root">
            <ul role="list" className="divide-y divide-border">
                {cart.products.map((product) => (
                    <CartProductItem key={product.id} product={product} onRemove={removeProduct}/>
                ))}
            </ul>
        </div>
    );
};

const CartProductItem = ({product, onRemove}: { product: Product, onRemove: (sku: string) => void }) => {
    const t = useTranslations('COMPONENTS.CART');

    const href = useMemo(() => {
        return product.description ? `/product/${product.description.friendlyUrl}` : "#";
    }, [product.description]);

    const image = product.images?.[0];

    return (
        <li className="py-4">
            <div className="flex items-start gap-4">
                <div className="relative h-20 w-16 shrink-0 overflow-hidden rounded-xl border border-border bg-secondary/30">
                    {product.description ? (
                        <Link prefetch={false} href={href} className="block h-full w-full">
                            {image && (
                                <Image
                                    alt={image.imageName}
                                    src={image.imageUrl}
                                    fill
                                    className="object-cover"
                                    sizes="64px"
                                />
                            )}
                        </Link>
                    ) : (
                        image && (
                            <Image
                                alt={image.imageName}
                                src={image.imageUrl}
                                fill
                                className="object-cover"
                                sizes="64px"
                            />
                        )
                    )}
                </div>

                <div className="min-w-0 flex-1">
                    <div className="flex items-start justify-between gap-3">
                        <div className="min-w-0">
                            {product.description ? (
                                <Link
                                    prefetch={false}
                                    href={href}
                                    className="block text-sm font-medium tracking-wide text-foreground hover:underline underline-offset-4"
                                >
                                    <span className="line-clamp-2">{product.description.name}</span>
                                </Link>
                            ) : (
                                <p className="text-sm font-medium text-foreground">Product</p>
                            )}

                            <div className="mt-1 flex items-center gap-3 text-xs text-muted-foreground">
                                <span className="tabular-nums">{t('QTY')}: {product.quantity}</span>
                                {product.sku && (
                                    <span className="truncate">SKU: {product.sku}</span>
                                )}
                            </div>
                        </div>

                        <div className="shrink-0 text-end">
                            <p className="text-sm font-semibold text-foreground tabular-nums">
                                {product.finalPrice}
                            </p>
                        </div>
                    </div>

                    <div className="mt-3 flex items-center justify-between">
                        <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            onClick={() => onRemove(product.sku)}
                            className={cn("rounded-full")}
                        >
                            {t('REMOVE')}
                        </Button>
                    </div>
                </div>
            </div>
        </li>
    );
};