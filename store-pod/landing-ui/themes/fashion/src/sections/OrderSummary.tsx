'use client'
import {useTranslations} from 'next-intl';
import type {StoreContext} from '@store-front/types';
import {useCart} from '@store-front/hooks/use-cart';
import {CartLineItem} from '../components/CartLineItem';
import {EmptyState} from '../states/EmptyState';

/** Checkout sidebar: the bag's lines as small sheets on a sheet, the subtotal printed at the foot. */
export function OrderSummary({storeContext}: { storeContext: StoreContext }) {
    const t = useTranslations('PAGE.CHECKOUT');
    const {cart, isEmpty, updateQuantity, removeProduct, status} = useCart(storeContext);
    return (
        <aside className="sheet p-4 [--tilt:0.5deg] lg:sticky lg:top-[calc(var(--header-h-lg)+1.5rem)]" aria-label={t('CART_DETAILS')}>
            <h2 className="mb-3 font-display text-xl uppercase tracking-wide">{t('CART_DETAILS')}</h2>
            {isEmpty ? <EmptyState kind="cart"/> : (
                <>
                    <ul className="wall wall-calm flex flex-col gap-3">
                        {cart!.products!.map(p => <CartLineItem key={p.id} product={p} onQuantity={updateQuantity} onRemove={removeProduct} busy={status === 'busy'}/>)}
                    </ul>
                    <div className="rule mt-4 flex items-center justify-between border-t pt-3 font-display text-lg uppercase">
                        <span>{t('SUB_TOTAL')}</span><span className="tabular-nums">{cart!.displaySubTotal}</span>
                    </div>
                </>
            )}
        </aside>
    );
}
