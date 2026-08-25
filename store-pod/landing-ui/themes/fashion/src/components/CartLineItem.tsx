'use client'
import {useTranslations} from 'next-intl';
import {XIcon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {Product} from '@store-front/types';
import {primaryImage, productHref} from '@store-front/services/product-presenter';
import {QuantityStepper} from '@store-front/ui/quantity-stepper';
import {PosterImage} from './PosterImage';

/** One line in the bag: a thumbnail sheet, the name in poster caps, the stepper, the line total. */
export function CartLineItem({product, onQuantity, onRemove, busy}: {
    product: Product; onQuantity: (sku: string, qty: number) => void; onRemove: (sku: string) => void; busy?: boolean
}) {
    const t = useTranslations('COMPONENTS.CART');
    const img = primaryImage(product);
    const name = product.description?.name ?? '';
    return (
        <li className="sheet sheet-flat flex gap-3 p-2.5">
            <Link prefetch={false} href={productHref(product)} className="relative size-20 shrink-0 overflow-hidden bg-background shadow-sm">
                <PosterImage src={img.src} alt={img.alt} title={name} tone="faint" sizes="80px"/>
            </Link>
            <div className="flex min-w-0 flex-1 flex-col gap-2">
                <div className="flex items-start justify-between gap-3">
                    <h3 className="line-clamp-2 font-display text-base uppercase leading-[0.95]" dir="auto">
                        <Link prefetch={false} href={productHref(product)}><bdi>{name}</bdi></Link>
                    </h3>
                    <button type="button" onClick={() => onRemove(product.sku)} disabled={busy} aria-label={t('REMOVE')}
                            className="-me-1 -mt-1 flex size-7 shrink-0 items-center justify-center text-muted-foreground hover:bg-foreground hover:text-background disabled:opacity-50">
                        <XIcon className="size-4"/>
                    </button>
                </div>
                <div className="mt-auto flex items-center justify-between gap-3">
                    <QuantityStepper size="sm" value={product.quantity}
                                     onDecrement={() => onQuantity(product.sku, product.quantity - 1)}
                                     onIncrement={() => onQuantity(product.sku, product.quantity + 1)}
                                     canDecrement={!busy} canIncrement={!busy}
                                     decrementLabel={t('DECREASE_QUANTITY')} incrementLabel={t('INCREASE_QUANTITY')}
                                     className="[&_button]:rounded-none [&_button]:border-foreground/40"/>
                    <p className="text-sm font-bold tabular-nums">{product.displaySubTotal || product.finalPrice}</p>
                </div>
            </div>
        </li>
    );
}
