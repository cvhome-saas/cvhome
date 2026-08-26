'use client'
import {useEffect, useRef, useState} from 'react';
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import type {Product, StoreContext} from '@store-front/types';
import {hasVariants, isOutOfStock, primaryImage, productHref} from '@store-front/services/product-presenter';
import {useCart} from '@store-front/hooks/use-cart';
import {Price} from '@store-front/ui/price';
import {cn} from '@store-front/ui/lib/utils';
import {ProductBadges} from './ProductBadges';

/**
 * One dish, printed as a menu line: the order number you would say out loud, a small printed thumb when
 * the merchant supplied a picture, the name with a dotted leader running to the price, and ADD at the end.
 * Adding runs the one authored moment — the second plate comes down across the line — and leaves the
 * number box filled with the quantity now in the order, so a long menu shows what you already chose.
 */
export function ProductCard({product, storeContext, priority, variant = 'line', className}: {
    product: Product; storeContext: StoreContext; priority?: boolean; variant?: 'line' | 'board'; className?: string
}) {
    const board = variant === 'board';
    const t = useTranslations('COMPONENTS.PRODUCT');
    const tp = useTranslations('PAGE.PRODUCT');
    const {addToCart, status, cart} = useCart(storeContext);
    const img = primaryImage(product);
    const hasImage = !!product.images?.length;
    const href = productHref(product);
    const out = isOutOfStock(product);
    const variants = hasVariants(product);
    const ordered = cart?.products?.find(p => p.sku === product.sku)?.quantity ?? 0;

    // the impression: shown for one press cycle after this line's own add resolves
    const [pressed, setPressed] = useState(false);
    const timer = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);
    useEffect(() => () => clearTimeout(timer.current), []);
    const add = async () => {
        await addToCart(product.sku, 1);
        setPressed(true);
        clearTimeout(timer.current);
        timer.current = setTimeout(() => setPressed(false), 520);
    };

    return (
        <article className={cn('dish crop grid grid-cols-[auto_minmax(0,1fr)] items-start gap-x-3 gap-y-2 sm:grid-cols-[auto_minmax(0,1fr)_auto] sm:gap-x-4',
            board ? 'py-5' : 'py-3.5', out && 'opacity-70', className)}>
            {pressed && <span className="impression" aria-hidden>{t('ADDED')}</span>}

            <div className="col-start-1 flex items-center gap-3 sm:row-span-2">
                {product.sku && (
                    <span className="dish-no" data-ordered={ordered > 0 ? 'true' : undefined} dir="ltr"
                          title={`${t('CATALOGUE_NO')} ${product.sku}`}>
                        {ordered > 0 ? `×${ordered}` : orderCode(product.sku)}
                    </span>
                )}
                {hasImage && (
                    <Link prefetch={false} href={href} tabIndex={-1} aria-hidden
                          className={cn('relative block shrink-0 overflow-hidden border border-border bg-muted',
                              board ? 'size-16 sm:size-24' : 'size-11 sm:size-14')}>
                        <Image src={img.src} alt="" fill sizes={board ? '96px' : '56px'} priority={priority} className="object-cover"/>
                    </Link>
                )}
            </div>

            <div className="col-start-2 flex min-w-0 flex-col gap-1">
                <div className="flex items-baseline gap-2">
                    <h3 className={cn('dish-name min-w-0', board ? 'text-xl sm:text-2xl lg:text-3xl' : 'text-base sm:text-lg')}>
                        <Link prefetch={false} href={href} className="hover:underline">{product.description?.name}</Link>
                    </h3>
                    <span aria-hidden className="leader hidden sm:block"/>
                </div>
                {product.description?.description && (
                    <p className={cn('line-clamp-2 max-w-[68ch] leading-snug text-muted-foreground', board ? 'text-sm' : 'text-xs')}>
                        {stripTags(product.description.description)}
                    </p>
                )}
                <ProductBadges product={product} className="flex flex-wrap items-center gap-1.5 pt-0.5"/>
            </div>

            {/* On a phone the price and the action drop to their own full-width row: squeezing a tabular
                price and an ADD plate into a third column collapses the dish name, and worst in Arabic. */}
            <div className="col-span-2 col-start-1 flex shrink-0 items-center justify-between gap-3 border-t border-dotted border-border pt-2
                            sm:col-span-1 sm:col-start-3 sm:row-span-2 sm:flex-col sm:items-end sm:justify-start sm:border-0 sm:pt-0">
                <Price className={cn('price', board ? 'text-xl sm:text-2xl' : 'text-base sm:text-lg')}
                       finalPrice={product.productPrice?.finalPrice ?? product.finalPrice}
                       originalPrice={product.productPrice?.originalPrice}
                       discounted={product.productPrice?.discounted} size="sm"/>
                {variants ? (
                    <Link prefetch={false} href={href} className="fold h-9 min-w-24 justify-center text-xs">{t('VIEW_DETAILS')}</Link>
                ) : out ? (
                    <span className="mark mark-out h-9 min-w-24 justify-center">{tp('OUT_OF_STOCK')}</span>
                ) : (
                    <button type="button" onClick={add} disabled={status === 'busy'}
                            className="fold plate h-9 min-w-24 justify-center border-primary text-xs disabled:opacity-60"
                            aria-label={`${t('ADD_TO_CART')} — ${product.description?.name ?? ''}`}>
                        {t('ADD_TO_CART')}
                    </button>
                )}
            </div>
        </article>
    );
}

/**
 * The number you would order by. Long warehouse SKUs are printed as their last two segments so the code
 * column stays a column; the full SKU rides in the line's `title` and is printed in full on the dish page.
 */
function orderCode(sku: string) {
    const parts = sku.split('-').filter(Boolean);
    return parts.length > 2 ? parts.slice(-2).join('-') : sku;
}

/** Menu copy comes through the CMS as HTML; the line only ever prints one plain sentence of it. */
function stripTags(html: string) {
    return html.replace(/<[^>]*>/g, ' ').replace(/&nbsp;/g, ' ').replace(/\s+/g, ' ').trim();
}
