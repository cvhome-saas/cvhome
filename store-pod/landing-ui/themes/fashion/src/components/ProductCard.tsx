'use client'
import {useEffect, useState} from 'react';
import {useTranslations} from 'next-intl';
import {ArrowRightIcon, PlusIcon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {Product, StoreContext} from '@store-front/types';
import {hasVariants, isOutOfStock, primaryImage, productHref, secondaryImage} from '@store-front/services/product-presenter';
import {useCart} from '@store-front/hooks/use-cart';
import {AspectBox} from '@store-front/ui/aspect-box';
import {cn} from '@store-front/ui/lib/utils';
import {PosterImage} from './PosterImage';
import {ProductBadges} from './ProductBadges';

/**
 * A product poster: picture on paper (second picture on hover), stamps over the picture, the name in poster
 * caps, the price printed on the foot beside a day-glo add square. Adding slaps an ADDED stamp on the poster.
 * Products with variants go to their page instead (the square becomes an arrow).
 *
 * `heading` is the level the name is set at. h3 suits a card sitting under a section's h2; the hero wall
 * pastes a poster straight under the store's h1, where h3 would skip a level.
 */
export function ProductCard({product, storeContext, priority, className, heading: Heading = 'h3'}: {
    product: Product; storeContext: StoreContext; priority?: boolean; className?: string; heading?: 'h2' | 'h3'
}) {
    const t = useTranslations('COMPONENTS.PRODUCT');
    const {addToCart, status, count} = useCart(storeContext);
    const [stampKey, setStampKey] = useState(0);
    const [pending, setPending] = useState<number | null>(null);  // the cart count when this poster's add was clicked
    const [sawBusy, setSawBusy] = useState(false);
    if (pending !== null && count !== pending) {  // the bag took it: slap the stamp once
        setPending(null); setSawBusy(false);
        if (count > pending) setStampKey(k => k + 1);
    } else if (pending !== null && status === 'busy' && !sawBusy) {
        setSawBusy(true);
    } else if (pending !== null && sawBusy && status === 'idle') {  // settled without a new line (failed, or quantity merged): no stamp
        setPending(null); setSawBusy(false);
    }
    const img = primaryImage(product);
    const hover = secondaryImage(product);
    const href = productHref(product);
    const out = isOutOfStock(product);
    const variants = hasVariants(product);
    const name = product.description?.name ?? '';
    const price = product.productPrice;
    const brand = product.manufacturer?.description?.name;

    useEffect(() => {
        if (!stampKey) return;
        const id = setTimeout(() => setStampKey(0), 1800);
        return () => clearTimeout(id);
    }, [stampKey]);

    return (
        <article className={cn('sheet sheet-lift group relative flex h-full flex-col', className)}>
            <Link prefetch={false} href={href} className="relative block overflow-hidden" aria-label={name}>
                <AspectBox className={cn('bg-background', out && 'bleached')}>
                    <PosterImage src={img.src} alt={img.alt} title={name} tone="faint" priority={priority}
                                 sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 17vw"
                                 className={cn('transition-opacity duration-(--motion-base)', hover && 'group-hover:opacity-0')}/>
                    {hover && (
                        <PosterImage src={hover.src} alt="" title={name} tone="faint" sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 17vw"
                                     className="opacity-0 transition-opacity duration-(--motion-base) group-hover:opacity-100"/>
                    )}
                </AspectBox>
                <ProductBadges product={product} className="absolute start-2 top-3 flex flex-col items-start gap-2"/>
                {stampKey > 0 && (
                    <span key={stampKey} className="stamp stamp-glo stamp-lg stamp-slap absolute start-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 rtl:translate-x-1/2" aria-hidden>
                        {t('ADDED')}
                    </span>
                )}
            </Link>
            <div className="flex flex-1 flex-col px-3 pb-3 pt-2.5">
                <Heading className="line-clamp-2 font-display text-lg uppercase leading-[0.95]" dir="auto">
                    <Link prefetch={false} href={href} className="hover:underline hover:decoration-primary hover:decoration-[3px] hover:underline-offset-4"><bdi>{name}</bdi></Link>
                </Heading>
                {brand && <p className="mt-1 truncate text-xs uppercase tracking-wide text-muted-foreground">{brand}</p>}
                <div className="mt-auto flex items-end justify-between gap-2 pt-3">
                    <p className="flex flex-col leading-none tabular-nums">
                        {price?.discounted && price.originalPrice && <del className="text-xs text-muted-foreground">{price.originalPrice}</del>}
                        <span className="text-base font-bold">{price?.finalPrice ?? product.finalPrice}</span>
                    </p>
                    {variants ? (
                        <Link prefetch={false} href={href} className="glo size-9 shrink-0" aria-label={t('VIEW_DETAILS')}>
                            <ArrowRightIcon className="size-4 rtl:rotate-180"/>
                        </Link>
                    ) : (
                        <button type="button" className="glo size-9 shrink-0" disabled={out || status === 'busy'} aria-label={out ? t('OUT_OF_STOCK') : t('ADD_TO_CART')}
                                onClick={() => { setPending(count); addToCart(product.sku, 1); }}>
                            <PlusIcon className="size-4"/>
                        </button>
                    )}
                </div>
            </div>
        </article>
    );
}
