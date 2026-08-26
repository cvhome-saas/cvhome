'use client'
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import {ShoppingBagIcon} from 'lucide-react';
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
 * One product in a grid or rail. Image (placeholder fallback, hover second image), badges, name (2-line
 * clamp), price pair, quick add (disabled out of stock; products with variants go to the page instead).
 */
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

    return (
        <article className={cn('group flex flex-col gap-3', className)}>
            <Link prefetch={false} href={href} className="relative block rounded-image" aria-label={product.description?.name}>
                <AspectBox className="rounded-image bg-muted">
                    <Image src={img.src} alt={img.alt} fill sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 25vw"
                           priority={priority}
                           className={cn('object-cover transition-opacity duration-(--motion-base)', hover && 'group-hover:opacity-0', out && 'opacity-60')}/>
                    {hover && (
                        <Image src={hover.src} alt="" aria-hidden fill sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 25vw"
                               className="object-cover opacity-0 transition-opacity duration-(--motion-base) group-hover:opacity-100"/>
                    )}
                </AspectBox>
                <ProductBadges product={product} className="absolute start-2 top-2 flex flex-col items-start gap-1"/>
            </Link>
            <div className="flex flex-1 flex-col gap-1">
                <h3 className="line-clamp-2 text-sm font-medium leading-snug">
                    <Link prefetch={false} href={href} className="hover:underline">{product.description?.name}</Link>
                </h3>
                <Price finalPrice={product.productPrice?.finalPrice ?? product.finalPrice}
                       originalPrice={product.productPrice?.originalPrice}
                       discounted={product.productPrice?.discounted} size="sm"/>
            </div>
            {variants ? (
                <Button asChild variant="outline" size="sm" className="w-full">
                    <Link prefetch={false} href={href}>{t('VIEW_DETAILS')}</Link>
                </Button>
            ) : (
                <Button variant="outline" size="sm" className="w-full" disabled={out || status === 'busy'}
                        onClick={() => addToCart(product.sku, 1)}>
                    <ShoppingBagIcon data-icon="inline-start"/>
                    {out ? t('OUT_OF_STOCK') : t('ADD_TO_CART')}
                </Button>
            )}
        </article>
    );
}
