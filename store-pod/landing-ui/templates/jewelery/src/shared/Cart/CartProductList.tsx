'use client'
import {Cart} from "@/types/cart";
import {Link} from "@/i18n/navigation";
import {StoreContext} from "@/types/store-context";
import Image from 'next/image';
import {useTranslations} from "next-intl";
import React from "react";
import {Product} from "@store-front/types";
import {useCart} from "@store-front/hooks/use-cart";

export const CartProductList = ({storeContext, cart}: {
    storeContext: StoreContext,
    cart: Cart | undefined,
}) => {
    const {removeProduct} = useCart(storeContext);

    if (!cart || !cart.products || cart.products.length === 0) {
        return (
            <div className="flex flex-col items-center justify-center py-16 text-center">
                <p className="text-xs tracking-widest uppercase text-muted-foreground">Your cart is empty</p>
            </div>
        );
    }

    return (
        <ul role="list" className="divide-y divide-border">
            {cart.products.map((product) => (
                <CartProductItem key={product.id} product={product} onRemove={removeProduct}/>
            ))}
        </ul>
    );
};

const CartProductItem = ({product, onRemove}: { product: Product, onRemove: (sku: string) => void }) => {
    const t = useTranslations('COMPONENTS.CART');

    return (
        <li className="flex gap-4 py-5">
            {/* Thumbnail */}
            {product.description && (
                <div className="relative w-20 h-24 shrink-0 overflow-hidden bg-secondary border border-border">
                    <Link prefetch={false} href={`/product/${product.description.friendlyUrl}`}
                          className="block w-full h-full">
                        {product.images && product.images.length > 0 && (
                            <Image
                                alt={product.images[0].imageName}
                                src={product.images[0].imageUrl}
                                fill
                                style={{objectFit: 'cover'}}
                                sizes="80px"
                            />
                        )}
                    </Link>
                </div>
            )}

            {/* Info */}
            <div className="flex flex-1 flex-col gap-2 min-w-0">
                {product.description && (
                    <Link
                        prefetch={false}
                        href={`/product/${product.description.friendlyUrl}`}
                        className="font-['Cormorant_Garamond',serif] text-sm font-light tracking-wide text-foreground hover:text-primary transition-colors line-clamp-2 leading-snug"
                    >
                        {product.description.name}
                    </Link>
                )}
                <p className="text-sm font-medium tracking-wider text-foreground">
                    {product.finalPrice}
                </p>
                <div className="flex items-center justify-between mt-auto">
                    <span className="text-[10px] tracking-widest uppercase text-muted-foreground">
                        {t('QTY')}: {product.quantity}
                    </span>
                    <button
                        type="button"
                        onClick={() => onRemove(product.sku)}
                        className="text-[10px] tracking-[0.15em] uppercase text-muted-foreground hover:text-destructive transition-colors duration-200"
                    >
                        {t('REMOVE')}
                    </button>
                </div>
            </div>
        </li>
    );
};
