'use client'
import {useTranslations} from 'next-intl';
import {XIcon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {Product} from '@store-front/types';
import {productHref} from '@store-front/services/product-presenter';
import {QuantityStepper} from '@store-front/ui/quantity-stepper';

/**
 * A line on the order docket: number, dish, quantity, line total in the same tabular column the menu uses.
 * No thumbnail — a docket is written, not illustrated.
 */
export function CartLineItem({product, onQuantity, onRemove, busy}: {
    product: Product; onQuantity: (sku: string, qty: number) => void; onRemove: (sku: string) => void; busy?: boolean
}) {
    const t = useTranslations('COMPONENTS.CART');
    return (
        <li className="grid grid-cols-[auto_1fr_auto] items-start gap-x-3 gap-y-2 border-b border-border py-3">
            {product.sku && <span className="dish-no col-start-1 row-span-2" dir="ltr">{product.sku}</span>}
            <h3 className="col-start-2 text-sm font-semibold leading-snug">
                <Link prefetch={false} href={productHref(product)} className="hover:underline">{product.description?.name}</Link>
            </h3>
            <p className="price col-start-3 text-base">{product.displaySubTotal || product.finalPrice}</p>
            <div className="col-start-2 col-end-4 flex items-center gap-3">
                <QuantityStepper size="sm" value={product.quantity}
                                 onDecrement={() => onQuantity(product.sku, product.quantity - 1)}
                                 onIncrement={() => onQuantity(product.sku, product.quantity + 1)}
                                 canDecrement={!busy} canIncrement={!busy}
                                 decrementLabel={t('DECREASE_QUANTITY')} incrementLabel={t('INCREASE_QUANTITY')}/>
                <button type="button" onClick={() => onRemove(product.sku)} disabled={busy}
                        className="press ms-auto inline-flex items-center gap-1 text-xs tracking-wide text-muted-foreground hover:text-foreground disabled:opacity-50">
                    <XIcon className="size-3.5"/>{t('REMOVE')}
                </button>
            </div>
        </li>
    );
}
