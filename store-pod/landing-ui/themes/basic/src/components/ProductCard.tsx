'use client'
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import {ArrowRightIcon, PlusIcon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {Product, StoreContext} from '@store-front/types';
import {hasVariants, isOutOfStock, primaryImage, productHref, secondaryImage} from '@store-front/services/product-presenter';
import {useCart} from '@store-front/hooks/use-cart';
import {AspectBox} from '@store-front/ui/aspect-box';
import {cn} from '@store-front/ui/lib/utils';
import {ProductBadges} from './ProductBadges';
import {useAddedFlash} from './use-added';

/**
 * A catalogue entry: the photo (second photo on hover), state stamped over it, then the fixed slots —
 * name (2 lines), catalogue number, the price in the condensed voice, and the add square on the rule.
 * Adding floods the price cell green and prints ADDED. Products with variants go to their page instead.
 */
export function ProductCard({product, storeContext, priority, className}: {
    product: Product; storeContext: StoreContext; priority?: boolean; className?: string
}) {
    const t = useTranslations('COMPONENTS.PRODUCT');
    const {addToCart, status, cart} = useCart(storeContext);
    // total quantity, not line count: adding a product that is already in the cart must flash too
    const quantity = cart?.products?.reduce((n, line) => n + (line.quantity ?? 0), 0) ?? 0;
    const flash = useAddedFlash(quantity, status);
    const img = primaryImage(product);
    const hover = secondaryImage(product);
    const href = productHref(product);
    const out = isOutOfStock(product);
    const variants = hasVariants(product);
    const name = product.description?.name ?? '';
    const price = product.productPrice;
    const final = price?.finalPrice ?? product.finalPrice;

    return (
        <article className={cn('group relative flex h-full flex-col', className)}>
            <Link prefetch={false} href={href} className="relative block" aria-label={name}>
                <AspectBox className="bg-card">
                    <Image src={img.src} alt={img.alt} fill sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 25vw" priority={priority}
                           className={cn('object-cover transition-opacity duration-(--motion-base)', hover && 'group-hover:opacity-0', out && 'opacity-50 grayscale')}/>
                    {hover && (
                        <Image src={hover.src} alt="" aria-hidden fill sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 25vw"
                               className="object-cover opacity-0 transition-opacity duration-(--motion-base) group-hover:opacity-100"/>
                    )}
                </AspectBox>
                <ProductBadges product={product} className="absolute start-2 top-2 flex flex-col items-start gap-1"/>
            </Link>
            <div className="flex flex-1 flex-col gap-1 border-t px-3 pb-3 pt-2.5">
                <h3 className="line-clamp-2 font-sans text-sm font-medium leading-snug" dir="auto">
                    <Link prefetch={false} href={href} className="hover:underline"><bdi>{name}</bdi></Link>
                </h3>
                {product.sku && <p className="cat-no">{t('CATALOGUE_NO')} <span dir="ltr">{product.sku}</span></p>}
                <div className="mt-auto flex items-end justify-between gap-2 pt-2">
                    <div className="flash -mx-1 min-w-0 px-1" data-flash={flash.on ? 'on' : undefined}>
                        {flash.on ? (
                            <span className="price block text-xl text-success-foreground" role="status">{t('ADDED')}</span>
                        ) : (
                            <p className="flex flex-col">
                                {price?.discounted && price.originalPrice && <del className="text-xs tabular-nums text-muted-foreground">{price.originalPrice}</del>}
                                <span className="price text-xl">{final}</span>
                            </p>
                        )}
                    </div>
                    {variants ? (
                        <Link prefetch={false} href={href} className="chip size-9 shrink-0 justify-center p-0" aria-label={t('VIEW_DETAILS')}>
                            <ArrowRightIcon className="size-4 rtl:rotate-180"/>
                        </Link>
                    ) : (
                        <button type="button" className="chip size-9 shrink-0 justify-center p-0 disabled:cursor-not-allowed disabled:opacity-50"
                                disabled={out || status === 'busy'} aria-label={out ? t('OUT_OF_STOCK') : t('ADD_TO_CART')}
                                onClick={() => { flash.arm(); addToCart(product.sku, 1); }}>
                            <PlusIcon className="size-4"/>
                        </button>
                    )}
                </div>
            </div>
        </article>
    );
}
