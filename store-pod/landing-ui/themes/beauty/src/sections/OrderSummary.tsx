'use client'
import {useTranslations} from 'next-intl';
import type {StoreContext} from '@store-front/types';
import {useCart} from '@store-front/hooks/use-cart';
import {CartLineItem} from '../components/CartLineItem';
import {EmptyState} from '../states/EmptyState';

/** Checkout sidebar: the bin contents on a plate, editable, subtotal in mono. */
export function OrderSummary({storeContext}: { storeContext: StoreContext }) {
    const t = useTranslations('PAGE.CHECKOUT');
    const {cart, isEmpty, updateQuantity, removeProduct, status} = useCart(storeContext);
    return (
        <aside className="plate lg:sticky lg:top-[calc(var(--header-h-lg)+1rem)]" aria-label={t('CART_DETAILS')}>
            <h2 className="q border-b border-foreground px-4 py-2 font-display text-base font-semibold uppercase tracking-wide">{t('CART_DETAILS')}</h2>
            <div className="px-4">
                {isEmpty ? <EmptyState kind="cart"/> : (
                    <ul className="divide-y divide-foreground">
                        {cart!.products!.map(p => <CartLineItem key={p.id} product={p} onQuantity={updateQuantity} onRemove={removeProduct} busy={status === 'busy'}/>)}
                    </ul>
                )}
            </div>
            {!isEmpty && (
                <>
                    <div className="hazard-soft h-2 border-t border-foreground" aria-hidden/>
                    <div className="flex items-baseline justify-between px-4 py-3 font-mono uppercase tracking-wide">
                        <span className="text-xs text-muted-foreground">{t('SUB_TOTAL')}</span>
                        <span className="text-lg font-bold">{cart!.displaySubTotal}</span>
                    </div>
                </>
            )}
        </aside>
    );
}
