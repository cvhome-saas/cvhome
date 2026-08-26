'use client'
import {useTranslations} from 'next-intl';
import type {StoreContext} from '@store-front/types';
import {useCart} from '@store-front/hooks/use-cart';
import {CartLineItem} from '../components/CartLineItem';
import {EmptyState} from '../states/EmptyState';

/** The docket kept beside the form while it is filled in: the same numbered lines, still editable. */
export function OrderSummary({storeContext}: { storeContext: StoreContext }) {
    const t = useTranslations('PAGE.CHECKOUT');
    const {cart, isEmpty, updateQuantity, removeProduct, status} = useCart(storeContext);
    return (
        <aside className="border-2 border-foreground lg:sticky lg:top-[calc(var(--header-h-lg)+1rem)]" aria-label={t('CART_DETAILS')}>
            <h2 className="plate press px-3 py-1.5 text-lg">{t('CART_DETAILS')}</h2>
            <div className="px-3 pb-3">
                {isEmpty ? <EmptyState kind="cart"/> : (
                    <>
                        <ul>
                            {cart!.products!.map(p => <CartLineItem key={p.id} product={p} onQuantity={updateQuantity} onRemove={removeProduct} busy={status === 'busy'}/>)}
                        </ul>
                        <div className="flex items-baseline justify-between gap-4 pt-3">
                            <span className="press text-base">{t('SUB_TOTAL')}</span>
                            <span className="price text-2xl">{cart!.displaySubTotal}</span>
                        </div>
                    </>
                )}
            </div>
        </aside>
    );
}
