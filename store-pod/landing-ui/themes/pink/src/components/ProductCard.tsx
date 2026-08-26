'use client'
import {useEffect, useState} from 'react';
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import type {Product, StoreContext} from '@store-front/types';
import {hasVariants, isOutOfStock, primaryImage, productHref, secondaryImage} from '@store-front/services/product-presenter';
import {useCart} from '@store-front/hooks/use-cart';
import {AspectBox} from '@store-front/ui/aspect-box';
import {Button} from '@store-front/ui/button';
import {Price} from '@store-front/ui/price';
import {cn} from '@store-front/ui/lib/utils';
import {ProductBadges} from './ProductBadges';

/**
 * One entry in the issue: a die-cut ranged in a ruled cell, its price printed on a flag that bleeds off
 * the picture's start edge, the pen's annotations over the top corners. Adding snaps an ADDED flag onto
 * the foot of the picture — clear of the annotation row, which owns the top —
 * as the issue's one authored motion; the cell then keeps saying how many are in the basket.
 */
export function ProductCard({product, storeContext, priority, className}: {
    product: Product; storeContext: StoreContext; priority?: boolean; className?: string
}) {
    const t = useTranslations('COMPONENTS.PRODUCT');
    const {cart, addToCart, status} = useCart(storeContext);
    const img = primaryImage(product);
    const hover = secondaryImage(product);
    const href = productHref(product);
    const out = isOutOfStock(product);
    const variants = hasVariants(product);

    // The basket is the truth: the flag snaps when this sku's quantity actually goes up.
    const inCart = cart?.products?.find(p => p.sku === product.sku)?.quantity ?? 0;
    const [seenInCart, setSeenInCart] = useState(inCart);
    const [flash, setFlash] = useState(0);
    if (inCart !== seenInCart) {  // adjust during render rather than in an effect
        setSeenInCart(inCart);
        if (inCart > seenInCart) setFlash(f => f + 1);
    }
    useEffect(() => {
        if (!flash) return;
        const id = setTimeout(() => setFlash(0), 1500);
        return () => clearTimeout(id);
    }, [flash]);

    return (
        <article className={cn('cell group flex h-full flex-col', className)}>
            <Link prefetch={false} href={href} className="relative block" aria-label={product.description?.name}>
                <AspectBox className="wash">
                    <Image src={img.src} alt={img.alt} fill sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 25vw"
                           priority={priority}
                           className={cn('object-cover transition-opacity duration-(--motion-base)', hover && 'group-hover:opacity-0', out && 'greyed')}/>
                    {hover && (
                        <Image src={hover.src} alt="" aria-hidden fill sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 25vw"
                               className={cn('object-cover opacity-0 transition-opacity duration-(--motion-base) group-hover:opacity-100', out && 'greyed')}/>
                    )}
                </AspectBox>
                <ProductBadges product={product} className="absolute start-2 top-2 end-2"/>
                {flash > 0 && (
                    <span key={flash} className="snap flag flag-flood absolute end-2 bottom-2 text-base" aria-hidden>{t('ADDED')}</span>
                )}
            </Link>

            <div className="hair flex flex-1 flex-col gap-2 border-t p-3">
                <h3 className="line-clamp-2 text-sm font-bold leading-snug">
                    <Link prefetch={false} href={href} className="hover:underline">{product.description?.name}</Link>
                </h3>
                <Price className="price-flag mt-auto"
                       finalPrice={product.productPrice?.finalPrice ?? product.finalPrice}
                       originalPrice={product.productPrice?.originalPrice}
                       discounted={product.productPrice?.discounted}/>
                {variants ? (
                    <Button asChild variant="outline" size="sm" className="w-full">
                        <Link prefetch={false} href={href}>{t('VIEW_DETAILS')}</Link>
                    </Button>
                ) : (
                    <Button size="sm" className="w-full" disabled={out || status === 'busy'}
                            onClick={() => addToCart(product.sku, 1)}>
                        {out ? t('OUT_OF_STOCK') : inCart > 0 ? t('IN_BASKET', {count: inCart}) : t('ADD_TO_CART')}
                    </Button>
                )}
            </div>
        </article>
    );
}
