'use client'
import {useState} from 'react';
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import {PlusIcon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {Product, StoreContext} from '@store-front/types';
import {hasVariants, isOutOfStock, primaryImage, productHref, secondaryImage} from '@store-front/services/product-presenter';
import {useCart} from '@store-front/hooks/use-cart';
import {AspectBox} from '@store-front/ui/aspect-box';
import {cn} from '@store-front/ui/lib/utils';
import {ProductBadges} from './ProductBadges';
import {TagButton} from './TagButton';

/**
 * An item plate: image window with a 1px rule, quoted name in display caps, a fixed facts band (brand · SKU),
 * the price in mono, and the add tag. Out of stock: the stripe runs across the window.
 */
export function ProductCard({product, storeContext, priority, className}: { product: Product; storeContext: StoreContext; priority?: boolean; className?: string }) {
    const t = useTranslations('COMPONENTS.PRODUCT');
    const tp = useTranslations('PAGE.PRODUCT');
    const {addToCart, status} = useCart(storeContext);
    const [swing, setSwing] = useState(false);
    const img = primaryImage(product);
    const hover = secondaryImage(product);
    const href = productHref(product);
    const out = isOutOfStock(product);
    const variants = hasVariants(product);
    const price = product.productPrice;

    return (
        <article className={cn('plate group relative flex h-full flex-col', className)}>
            <Link prefetch={false} href={href} className="relative block border-b border-foreground" aria-label={product.description?.name}>
                <AspectBox className="bg-muted">
                    <Image src={img.src} alt={img.alt} fill sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 25vw" priority={priority}
                           className={cn('object-cover transition-opacity duration-(--motion-base)', hover && 'group-hover:opacity-0', out && 'opacity-50')}/>
                    {hover && <Image src={hover.src} alt="" aria-hidden fill sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 25vw" className="object-cover opacity-0 transition-opacity duration-(--motion-base) group-hover:opacity-100"/>}
                    {out && <div className="hazard-soft absolute inset-0" aria-hidden/>}
                </AspectBox>
                <ProductBadges product={product} className="absolute start-0 top-2 flex flex-col items-start gap-1"/>
            </Link>
            <div className="flex flex-1 flex-col gap-2 p-3">
                <h3 className="line-clamp-2 font-display text-lg font-semibold uppercase leading-snug tracking-tight">
                    <Link prefetch={false} href={href} className="hover:underline hover:decoration-primary hover:decoration-2 hover:underline-offset-4"><span className="q" dir="auto"><bdi>{product.description?.name}</bdi></span></Link>
                </h3>
                <dl className="font-mono text-[0.7rem] uppercase leading-snug tracking-wide text-muted-foreground">
                    {product.manufacturer?.description?.name && <div className="flex gap-2"><dt className="sr-only">{tp('SPECIFICATIONS')}</dt><dd>{product.manufacturer.description.name}</dd></div>}
                    <div className="flex gap-2"><dt>{tp('SKU')}:</dt><dd dir="ltr">{product.sku}</dd></div>
                </dl>
                <div className="mt-auto flex flex-col gap-2 pt-2">
                    <p className="flex items-baseline gap-2 font-mono leading-none">
                        <span className="text-base font-bold">{price?.finalPrice ?? product.finalPrice}</span>
                        {price?.discounted && price.originalPrice && <del className="text-xs text-muted-foreground">{price.originalPrice}</del>}
                    </p>
                    {variants ? (
                        <TagButton asChild size="sm" className="w-full"><Link prefetch={false} href={href}>{t('VIEW_DETAILS')}</Link></TagButton>
                    ) : (
                        <TagButton size="sm" className="w-full whitespace-nowrap" swing={swing} disabled={out || status === 'busy'} aria-label={out ? t('OUT_OF_STOCK') : t('ADD_TO_CART')}
                                   onClick={() => { setSwing(false); requestAnimationFrame(() => setSwing(true)); addToCart(product.sku, 1); }}>
                            <PlusIcon className="size-4"/>{out ? t('OUT_OF_STOCK') : t('ADD_TO_CART')}
                        </TagButton>
                    )}
                </div>
            </div>
        </article>
    );
}
