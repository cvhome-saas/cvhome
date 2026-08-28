'use client'
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import {Trash2Icon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {Product} from '@store-front/types';
import {primaryImage, productHref} from '@store-front/services/product-presenter';
import {Button} from '@store-front/ui/button';
import {QuantityStepper} from '@store-front/ui/quantity-stepper';
import {Figure} from './Figure';

/** One line of the delivery docket: thumb, name, the quantity in its slot, the line total in the next. */
export function CartLineItem({product, onQuantity, onRemove, busy}: {
    product: Product; onQuantity: (sku: string, qty: number) => void; onRemove: (sku: string) => void; busy?: boolean
}) {
    const t = useTranslations('COMPONENTS.CART');
    const img = primaryImage(product);
    return (
        <li className="rule-brass flex gap-4 border-b py-4 last:border-b-0">
            <Link prefetch={false} href={productHref(product)} className="window relative size-20 shrink-0">
                <Image src={img.src} alt={img.alt} fill sizes="80px" className="object-cover"/>
            </Link>
            <div className="flex min-w-0 flex-1 flex-col gap-2">
                <div className="flex items-start justify-between gap-3">
                    <h3 className="line-clamp-2 text-sm leading-snug">
                        <Link prefetch={false} href={productHref(product)} className="hover:underline">{product.description?.name}</Link>
                    </h3>
                    <Figure value={product.displaySubTotal || product.finalPrice} className="shrink-0 text-sm"/>
                </div>
                <div className="flex items-center justify-between gap-3">
                    <QuantityStepper size="sm" value={product.quantity}
                                     onDecrement={() => onQuantity(product.sku, product.quantity - 1)}
                                     onIncrement={() => onQuantity(product.sku, product.quantity + 1)}
                                     canDecrement={!busy} canIncrement={!busy}
                                     decrementLabel={t('DECREASE_QUANTITY')} incrementLabel={t('INCREASE_QUANTITY')}/>
                    <Button variant="ghost" size="sm" onClick={() => onRemove(product.sku)} disabled={busy}
                            className="sign text-[0.625rem] text-muted-foreground">
                        <Trash2Icon data-icon="inline-start"/>{t('REMOVE')}
                    </Button>
                </div>
            </div>
        </li>
    );
}
