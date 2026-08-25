'use client'
import {useTranslations} from 'next-intl';
import type {StoreContext} from '@store-front/types';
import {useCart} from '@store-front/hooks/use-cart';
import {CartLineItem} from '../components/CartLineItem';
import {EmptyState} from '../states/EmptyState';

/** The order form's summary column: editable lines under a rule, the subtotal in the price voice. */
export function OrderSummary({storeContext}: { storeContext: StoreContext }) {
    const t = useTranslations('PAGE.CHECKOUT');
    const {cart, isEmpty, updateQuantity, removeProduct, status} = useCart(storeContext);
    return (
        <aside className="border bg-card text-card-foreground lg:sticky lg:top-[calc(var(--header-h-lg)+1rem)]" aria-label={t('CART_DETAILS')}>
            <h2 className="display border-b px-4 py-3 text-xl">{t('CART_DETAILS')}</h2>
            {isEmpty ? <EmptyState kind="cart"/> : (
                <>
                    <ul className="divide-y">
                        {cart!.products!.map(p => <CartLineItem key={p.id} product={p} onQuantity={updateQuantity} onRemove={removeProduct} busy={status === 'busy'}/>)}
                    </ul>
                    <div className="flex items-baseline justify-between gap-4 border-t px-4 py-4">
                        <span className="text-sm font-semibold uppercase tracking-wide text-muted-foreground">{t('SUB_TOTAL')}</span>
                        <span className="price text-2xl">{cart!.displaySubTotal}</span>
                    </div>
                </>
            )}
        </aside>
    );
}
