'use client'
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import {Trash2Icon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {Product} from '@store-front/types';
import {primaryImage, productHref} from '@store-front/services/product-presenter';
import {Button} from '@store-front/ui/button';
import {QuantityStepper} from '@store-front/ui/quantity-stepper';

export function CartLineItem({product, onQuantity, onRemove, busy}: {
    product: Product; onQuantity: (sku: string, qty: number) => void; onRemove: (sku: string) => void; busy?: boolean
}) {
    const t = useTranslations('COMPONENTS.CART');
    const img = primaryImage(product);
    return (
        <li className="flex gap-4 py-4">
            <Link prefetch={false} href={productHref(product)} className="relative size-20 shrink-0 overflow-hidden rounded-image bg-muted">
                <Image src={img.src} alt={img.alt} fill sizes="80px" className="object-cover"/>
            </Link>
            <div className="flex min-w-0 flex-1 flex-col gap-2">
                <div className="flex items-start justify-between gap-3">
                    <h3 className="line-clamp-2 text-sm font-medium">
                        <Link prefetch={false} href={productHref(product)}>{product.description?.name}</Link>
                    </h3>
                    <p className="shrink-0 text-sm font-semibold">{product.displaySubTotal || product.finalPrice}</p>
                </div>
                <div className="flex items-center justify-between gap-3">
                    <QuantityStepper size="sm" value={product.quantity}
                                     onDecrement={() => onQuantity(product.sku, product.quantity - 1)}
                                     onIncrement={() => onQuantity(product.sku, product.quantity + 1)}
                                     canDecrement={!busy} canIncrement={!busy}
                                     decrementLabel={t('DECREASE_QUANTITY')} incrementLabel={t('INCREASE_QUANTITY')}/>
                    <Button variant="ghost" size="sm" onClick={() => onRemove(product.sku)} disabled={busy} className="text-muted-foreground">
                        <Trash2Icon data-icon="inline-start"/>{t('REMOVE')}
                    </Button>
                </div>
            </div>
        </li>
    );
}
