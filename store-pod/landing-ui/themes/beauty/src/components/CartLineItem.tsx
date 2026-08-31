'use client'
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import {XIcon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {Product} from '@store-front/types';
import {primaryImage, productHref, variantSelectionLabel} from '@store-front/services/product-presenter';
import {QuantityStepper} from '@store-front/ui/quantity-stepper';

export function CartLineItem({product, onQuantity, onRemove, busy}: { product: Product; onQuantity: (sku: string, qty: number) => void; onRemove: (sku: string) => void; busy?: boolean }) {
    const t = useTranslations('COMPONENTS.CART');
    const img = primaryImage(product);
    return (
        <li className="flex gap-3 py-3">
            <Link prefetch={false} href={productHref(product)} className="plate relative size-20 shrink-0 overflow-hidden bg-muted">
                <Image src={img.src} alt={img.alt} fill sizes="80px" className="object-cover"/>
            </Link>
            <div className="flex min-w-0 flex-1 flex-col gap-2">
                <div className="flex items-start justify-between gap-3">
                    <h3 className="line-clamp-2 font-display text-base font-semibold uppercase leading-snug">
                        <Link prefetch={false} href={productHref(product)} ><span className="q" dir="auto"><bdi>{product.description?.name}</bdi></span></Link>
                    </h3>
                    {/* The combination bought — "Color: Red / Size: L" — from the placement-time selection. */}
                    {variantSelectionLabel(product) && <p className="text-xs text-muted-foreground" dir="auto">{variantSelectionLabel(product)}</p>}
                    <button type="button" onClick={() => onRemove(product.sku)} disabled={busy} aria-label={t('REMOVE')} className="plate flex size-6 shrink-0 items-center justify-center hover:bg-foreground hover:text-background disabled:opacity-50">
                        <XIcon className="size-3.5"/>
                    </button>
                </div>
                <p className="font-mono text-[0.7rem] uppercase tracking-wide text-muted-foreground" dir="ltr">{product.sku}</p>
                <div className="flex items-center justify-between gap-3">
                    <QuantityStepper size="sm" value={product.quantity}
                                     onDecrement={() => onQuantity(product.sku, product.quantity - 1)} onIncrement={() => onQuantity(product.sku, product.quantity + 1)}
                                     canDecrement={!busy} canIncrement={!busy} decrementLabel={t('DECREASE_QUANTITY')} incrementLabel={t('INCREASE_QUANTITY')}/>
                    <p className="font-mono text-sm font-bold">{product.displaySubTotal || product.finalPrice}</p>
                </div>
            </div>
        </li>
    );
}
