'use client'
import {useTranslations} from 'next-intl';
import type {StoreContext} from '@store-front/types';
import {useCart} from '@store-front/hooks/use-cart';
import {CartLineItem} from '../components/CartLineItem';
import {EmptyState} from '../states/EmptyState';

/** The order slip beside the form: the same ruled lines, the subtotal on the biggest flag. */
export function OrderSummary({storeContext}: { storeContext: StoreContext }) {
    const t = useTranslations('PAGE.CHECKOUT');
    const {cart, isEmpty, updateQuantity, removeProduct, status} = useCart(storeContext);
    return (
        <aside className="hair border-2 lg:sticky lg:top-[calc(var(--header-h-lg)+1rem)]" aria-label={t('CART_DETAILS')}>
            <h2 className="flood cover-line hair border-b-2 px-4 py-3">{t('CART_DETAILS')}</h2>
            <div className="ruled-stock px-4">
                {isEmpty ? <EmptyState kind="cart"/> : (
                    <ul>
                        {cart!.products!.map(p => <CartLineItem key={p.id} product={p} onQuantity={updateQuantity} onRemove={removeProduct} busy={status === 'busy'}/>)}
                    </ul>
                )}
            </div>
            {!isEmpty && (
                <div className="hair flex items-center justify-between gap-3 border-t-2 p-4">
                    <span className="cover-line">{t('SUB_TOTAL')}</span>
                    <span className="flag flag-lg">{cart!.displaySubTotal}</span>
                </div>
            )}
        </aside>
    );
}
