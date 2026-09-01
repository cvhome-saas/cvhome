'use client'
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import {XIcon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {Product} from '@store-front/types';
import {primaryImage, productHref, variantSelectionLabel} from '@store-front/services/product-presenter';
import {QuantityStepper} from '@store-front/ui/quantity-stepper';

/** One line of the order slip: die-cut, name, quantity, and the line total printed on its flag. */
export function CartLineItem({product, onQuantity, onRemove, busy}: {
    product: Product; onQuantity: (sku: string, qty: number) => void; onRemove: (sku: string) => void; busy?: boolean
}) {
    const t = useTranslations('COMPONENTS.CART');
    const img = primaryImage(product);
    return (
        <li className="hair flex gap-3 border-b bg-background py-3 last:border-b-0">
            <Link prefetch={false} href={productHref(product)} className="wash hair relative size-20 shrink-0 overflow-hidden border">
                <Image src={img.src} alt={img.alt} fill sizes="80px" className="object-cover"/>
            </Link>
            <div className="flex min-w-0 flex-1 flex-col gap-2">
                <div className="flex items-start justify-between gap-2">
                    <h3 className="line-clamp-2 text-sm font-bold leading-snug">
                        <Link prefetch={false} href={productHref(product)} className="hover:underline">{product.description?.name}</Link>
                    </h3>
                    {/* The combination bought — "Color: Red / Size: L" — from the placement-time selection. */}
                    {variantSelectionLabel(product) && <p className="text-xs text-muted-foreground" dir="auto">{variantSelectionLabel(product)}</p>}
                    <button type="button" onClick={() => onRemove(product.sku)} disabled={busy}
                            aria-label={t('REMOVE')}
                            className="hair relative -mt-0.5 inline-flex size-6 shrink-0 items-center justify-center border transition-colors duration-(--motion-fast) before:absolute before:-inset-2.5 before:content-[''] hover:bg-primary hover:text-primary-foreground disabled:opacity-40">
                        <XIcon className="size-3.5"/>
                    </button>
                </div>
                <div className="flex flex-wrap items-center justify-between gap-2">
                    <QuantityStepper size="sm" value={product.quantity}
                                     onDecrement={() => onQuantity(product.sku, product.quantity - 1)}
                                     onIncrement={() => onQuantity(product.sku, product.quantity + 1)}
                                     canDecrement={!busy} canIncrement={!busy}
                                     decrementLabel={t('DECREASE_QUANTITY')} incrementLabel={t('INCREASE_QUANTITY')}/>
                    <span className="flag">{product.displaySubTotal || product.finalPrice}</span>
                </div>
            </div>
        </li>
    );
}
