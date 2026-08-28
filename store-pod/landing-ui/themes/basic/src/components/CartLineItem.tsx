'use client'
import Image from 'next/image';
import {useTranslations} from 'next-intl';
import {Trash2Icon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {Product} from '@store-front/types';
import {primaryImage, productHref} from '@store-front/services/product-presenter';
import {Button} from '@store-front/ui/button';
import {QuantityStepper} from '@store-front/ui/quantity-stepper';

/** One line of the order form: thumbnail · name and number · quantity · line total in the price voice. */
export function CartLineItem({product, onQuantity, onRemove, busy}: {
    product: Product; onQuantity: (sku: string, qty: number) => void; onRemove: (sku: string) => void; busy?: boolean
}) {
    const t = useTranslations('COMPONENTS.CART');
    const tp = useTranslations('COMPONENTS.PRODUCT');
    const img = primaryImage(product);
    const href = productHref(product);
    return (
        <li className="flex gap-3 px-4 py-4">
            <Link prefetch={false} href={href} className="relative size-16 shrink-0 overflow-hidden border bg-card">
                <Image src={img.src} alt={img.alt} fill sizes="64px" className="object-cover"/>
            </Link>
            <div className="flex min-w-0 flex-1 flex-col gap-1.5">
                <div className="flex items-start justify-between gap-3">
                    <h3 className="line-clamp-2 font-sans text-sm font-medium leading-snug" dir="auto">
                        <Link prefetch={false} href={href} className="hover:underline"><bdi>{product.description?.name}</bdi></Link>
                    </h3>
                    <p className="price shrink-0 text-base">{product.displaySubTotal || product.finalPrice}</p>
                </div>
                {product.sku && <p className="cat-no">{tp('CATALOGUE_NO')} <span dir="ltr">{product.sku}</span></p>}
                <div className="mt-1 flex items-center justify-between gap-3">
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
