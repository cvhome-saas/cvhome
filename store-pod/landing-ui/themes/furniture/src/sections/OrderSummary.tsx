'use client'
import {useTranslations} from 'next-intl';
import type {StoreContext} from '@store-front/types';
import {useCart} from '@store-front/hooks/use-cart';
import {CartLineItem} from '../components/CartLineItem';
import {Figure} from '../components/Figure';
import {EmptyState} from '../states/EmptyState';

/** The docket beside the delivery desk: the same ruled lines as the drawer, still editable. */
export function OrderSummary({storeContext}: { storeContext: StoreContext }) {
    const t = useTranslations('PAGE.CHECKOUT');
    const {cart, isEmpty, updateQuantity, removeProduct, status} = useCart(storeContext);
    return (
        <aside className="rule-brass border lg:sticky lg:top-[calc(var(--header-h-lg)+1rem)]" aria-label={t('CART_DETAILS')}>
            <h2 className="enamel-flat sign-lg rounded-none px-5 py-3.5 text-base">{t('CART_DETAILS')}</h2>
            <div className="px-5">
                {isEmpty ? <EmptyState kind="cart"/> : (
                    <ul>
                        {cart!.products!.map(p => <CartLineItem key={p.id} product={p} onQuantity={updateQuantity} onRemove={removeProduct} busy={status === 'busy'}/>)}
                    </ul>
                )}
            </div>
            {!isEmpty && (
                <div className="rule-brass flex items-baseline justify-between border-t px-5 py-4">
                    <span className="sign text-[0.6875rem]">{t('SUB_TOTAL')}</span>
                    <Figure value={cart!.displaySubTotal ?? ''} className="text-xl"/>
                </div>
            )}
        </aside>
    );
}
