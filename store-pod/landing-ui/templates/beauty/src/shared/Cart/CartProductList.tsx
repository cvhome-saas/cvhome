'use client'
import {Cart} from "@/types/cart";
import {Link} from "@/i18n/navigation";
import {StoreContext} from "@/types/store-context";
import Image from 'next/image';
import {useTranslations} from "next-intl";
import {Button} from "@/components/ui/button";
import React from "react";
import {Product} from "@store-front/types";
import {useCart} from "@store-front/hooks/use-cart";

export const CartProductList = ({storeContext, cart}: {
    storeContext: StoreContext,
    cart: Cart | undefined,
}) => {
    const {removeProduct} = useCart(storeContext);

    return <div className="flow-root">
        <ul role="list" className="-my-6 divide-y divide-primary/10">
            {cart && cart.products && cart.products.map((product) => (
                <CartProductItem key={product.id} product={product} onRemove={removeProduct}/>
            ))}
        </ul>
    </div>;

}

const CartProductItem = ({product, onRemove}: { product: Product, onRemove: (sku: string) => void }) => {
    const t = useTranslations('COMPONENTS.CART');

    return (
        <li className="flex py-6 group">
            {product.description && (
                <div className="relative size-24 shrink-0 overflow-hidden rounded-2xl border border-primary/10 bg-secondary/10">
                    <Link prefetch={false} href={`/product/${product.description.friendlyUrl}`}
                          className="block size-full">
                        {product.images && product.images.length > 0 && (
                            <Image
                                alt={product.images[0].imageName}
                                src={product.images[0].imageUrl}
                                fill
                                style={{objectFit: 'cover'}}
                                className="transition-transform duration-500 group-hover:scale-110"
                                sizes="96px"
                            />
                        )}
                    </Link>
                </div>
            )}

            <div className="ms-4 flex flex-1 flex-col justify-between">
                <div>
                    <div className="flex justify-between text-base font-serif font-medium text-foreground/90">
                        {product.description && (
                            <h3>
                                <Link prefetch={false} href={`/product/${product.description.friendlyUrl}`} className="hover:text-primary transition-colors">
                                    {product.description.name}
                                </Link>
                            </h3>
                        )}
                        <p className="ms-4 font-sans font-bold text-primary/80">{product.finalPrice}</p>
                    </div>
                </div>

                <div className="flex items-center mt-2 w-full">
                    <span className="text-xs font-medium uppercase tracking-widest text-muted-foreground">{t('QTY')}: {product.quantity}</span>
                    <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        onClick={() => onRemove(product.sku)}
                        className="ms-auto text-xs uppercase tracking-tighter text-destructive/70 hover:text-destructive hover:bg-destructive/5 rounded-full"
                    >
                        {t('REMOVE')}
                    </Button>
                </div>
            </div>
        </li>
    );
}