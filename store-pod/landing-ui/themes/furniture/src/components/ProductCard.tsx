'use client'
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import type {Product, StoreContext} from '@store-front/types';
import {hasVariants, isOutOfStock, primaryImage, productHref, secondaryImage} from '@store-front/services/product-presenter';
import {useCart} from '@store-front/hooks/use-cart';
import {AspectBox} from '@store-front/ui/aspect-box';
import {Price} from '@store-front/ui/price';
import {cn} from '@store-front/ui/lib/utils';
import {ProductBadges} from './ProductBadges';

/**
 * One piece on the floor: the photograph cut square in a hairline window, the name read plainly, and the
 * price on its own small enamel plate — the same field the directory board is made of, repeated at grid
 * scale so colour commits across the whole page instead of hiding on one button.
 */
const PRICE_PLATE =
    'figure items-center gap-x-2 ' +
    '[&>ins]:rounded-card [&>ins]:bg-primary [&>ins]:px-2 [&>ins]:py-1 [&>ins]:text-primary-foreground ' +
    '[&>span]:rounded-card [&>span]:bg-primary [&>span]:px-2 [&>span]:py-1 [&>span]:text-primary-foreground ' +
    '[&>del]:text-xs [&>del]:text-muted-foreground';

export function ProductCard({product, storeContext, priority, className}: {
    product: Product; storeContext: StoreContext; priority?: boolean; className?: string
}) {
    const t = useTranslations('COMPONENTS.PRODUCT');
    const {addToCart, status} = useCart(storeContext);
    const img = primaryImage(product);
    const hover = secondaryImage(product);
    const href = productHref(product);
    const out = isOutOfStock(product);
    const variants = hasVariants(product);
    const sizes = '(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 25vw';

    return (
        <article className={cn('group flex h-full flex-col', className)}>
            <Link prefetch={false} href={href} className="relative block" aria-label={product.description?.name}>
                <AspectBox className="window">
                    <Image src={img.src} alt={img.alt} fill sizes={sizes} priority={priority}
                           className={cn('object-cover transition-opacity duration-(--motion-base) ease-standard',
                               hover && 'group-hover:opacity-0', out && 'opacity-55')}/>
                    {hover && (
                        <Image src={hover.src} alt="" aria-hidden fill sizes={sizes}
                               className="object-cover opacity-0 transition-opacity duration-(--motion-base) ease-standard group-hover:opacity-100"/>
                    )}
                </AspectBox>
                <ProductBadges product={product} className="absolute start-2 top-2 flex flex-col items-start gap-1"/>
            </Link>

            <div className="mt-3.5 flex flex-1 flex-col gap-2.5 sm:flex-row sm:items-start sm:justify-between sm:gap-3">
                <h3 className="line-clamp-2 flex-1 text-sm leading-snug">
                    <Link prefetch={false} href={href} className="hover:underline">{product.description?.name}</Link>
                </h3>
                <Price finalPrice={product.productPrice?.finalPrice ?? product.finalPrice}
                       originalPrice={product.productPrice?.originalPrice}
                       discounted={product.productPrice?.discounted}
                       size="sm" className={cn(PRICE_PLATE, 'shrink-0 items-start gap-y-1 sm:flex-col sm:items-end')}/>
            </div>

            {variants ? (
                <Link prefetch={false} href={href}
                      className="sign rule-brass mt-3.5 block rounded-control border py-2.5 text-center text-[0.625rem] transition-colors duration-(--motion-fast) hover:bg-primary hover:text-primary-foreground">
                    {t('VIEW_DETAILS')}
                </Link>
            ) : (
                <button type="button" disabled={out || status === 'busy'} onClick={() => addToCart(product.sku, 1)}
                        className="sign rule-brass mt-3.5 rounded-control border py-2.5 text-[0.625rem] transition-colors duration-(--motion-fast) hover:bg-primary hover:text-primary-foreground disabled:cursor-not-allowed disabled:border-[var(--brass-faint)] disabled:text-muted-foreground disabled:hover:bg-transparent">
                    {out ? t('OUT_OF_STOCK') : t('ADD_TO_CART')}
                </button>
            )}
        </article>
    );
}
