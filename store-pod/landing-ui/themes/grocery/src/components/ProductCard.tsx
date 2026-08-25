'use client'
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import {ArrowRightIcon, PlusIcon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {Product, StoreContext} from '@store-front/types';
import {hasVariants, isOutOfStock, primaryImage, productHref, secondaryImage} from '@store-front/services/product-presenter';
import {useCart} from '@store-front/hooks/use-cart';
import {AspectBox} from '@store-front/ui/aspect-box';
import {QuantityStepper} from '@store-front/ui/quantity-stepper';
import {cn} from '@store-front/ui/lib/utils';
import {ProductBadges} from './ProductBadges';
import {useAddedStamp} from './use-added';
import {useMounted} from './use-mounted';

/**
 * A crate: the photo (second photo on hover), state stickers slapped over it, then the rail tag — name
 * (2 lines), SKU, del original + the price in the signage voice — and the quick-add slot. Stepper-first:
 * a product already in the basket shows its stepper right in the crate; adding stamps ADDED over the tag.
 * Products with variants go to their page instead.
 */
export function ProductCard({product, storeContext, priority, className}: {
    product: Product; storeContext: StoreContext; priority?: boolean; className?: string
}) {
    const t = useTranslations('COMPONENTS.PRODUCT');
    const tp = useTranslations('PAGE.PRODUCT');
    const {addToCart, updateQuantity, status, cart, count} = useCart(storeContext);
    const mounted = useMounted();
    const stamp = useAddedStamp(count, status);
    const line = mounted ? cart?.products?.find(p => p.sku === product.sku) : undefined;
    const inBasket = (line?.quantity ?? 0) > 0;
    const img = primaryImage(product);
    const hover = secondaryImage(product);
    const href = productHref(product);
    const out = isOutOfStock(product);
    const variants = hasVariants(product);
    const busy = status === 'busy';
    const name = product.description?.name ?? '';
    const price = product.productPrice;
    const final = price?.finalPrice ?? product.finalPrice;

    return (
        <article className={cn('crate group relative flex h-full flex-col overflow-hidden', className)}>
            <Link prefetch={false} href={href} className="relative block" aria-label={name}>
                <AspectBox className="bg-card">
                    <Image src={img.src} alt={img.alt} fill sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 20vw" priority={priority}
                           className={cn('object-cover transition-opacity duration-(--motion-base)', hover && 'group-hover:opacity-0', out && 'opacity-50 grayscale')}/>
                    {hover && (
                        <Image src={hover.src} alt="" aria-hidden fill sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 20vw"
                               className="object-cover opacity-0 transition-opacity duration-(--motion-base) group-hover:opacity-100"/>
                    )}
                </AspectBox>
                <ProductBadges product={product} className="absolute start-2 top-2 flex flex-col items-start gap-1.5"/>
            </Link>
            <div className="flex flex-1 flex-col gap-1 border-t-2 px-3 pb-3 pt-2.5">
                <h3 className="line-clamp-2 text-sm font-semibold leading-snug" dir="auto">
                    <Link prefetch={false} href={href} className="hover:underline"><bdi>{name}</bdi></Link>
                </h3>
                {product.sku && (
                    <p className="font-mono text-xs text-muted-foreground">{tp('SKU')}: <span dir="ltr">{product.sku}</span></p>
                )}
                <div className="mt-auto flex flex-wrap items-end justify-between gap-x-2 gap-y-1.5 pt-2">
                    <div className="relative min-w-0">
                        {stamp.on && (
                            <span className="sticker sticker-success stamp-in absolute -top-1 start-0 z-10" data-tilt role="status">{t('ADDED')}</span>
                        )}
                        <p className="flex flex-col">
                            {price?.discounted && price.originalPrice && <del className="text-xs tabular-nums text-muted-foreground">{price.originalPrice}</del>}
                            <span className="price text-xl">{final}</span>
                        </p>
                    </div>
                    {variants ? (
                        <Link prefetch={false} href={href} aria-label={t('VIEW_DETAILS')}
                              className="flex size-10 shrink-0 items-center justify-center rounded-control border-2 bg-card transition-colors duration-(--motion-fast) hover:bg-muted">
                            <ArrowRightIcon className="size-4 rtl:rotate-180"/>
                        </Link>
                    ) : inBasket ? (
                        <QuantityStepper size="sm" value={line!.quantity} className="shrink-0"
                                         onDecrement={() => updateQuantity(product.sku, line!.quantity - 1)}
                                         onIncrement={() => { stamp.arm(); updateQuantity(product.sku, line!.quantity + 1); }}
                                         canDecrement={!busy} canIncrement={!busy && !out}
                                         decrementLabel={tp('DECREASE_QUANTITY')} incrementLabel={tp('INCREASE_QUANTITY')}/>
                    ) : (
                        <button type="button" disabled={out || busy} aria-label={out ? t('OUT_OF_STOCK') : t('ADD_TO_CART')}
                                onClick={() => { stamp.arm(); addToCart(product.sku, 1); }}
                                className="flex size-10 shrink-0 items-center justify-center rounded-control border-2 border-primary-hover bg-primary text-primary-foreground transition-colors duration-(--motion-fast) hover:bg-primary-hover disabled:cursor-not-allowed disabled:opacity-50">
                            <PlusIcon className="size-5"/>
                        </button>
                    )}
                </div>
            </div>
        </article>
    );
}
