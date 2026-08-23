'use client'
import {useTranslations} from 'next-intl';
import type {StoreContext} from '@store-front/types';
import {useCart} from '@store-front/hooks/use-cart';
import {Separator} from '@store-front/ui/separator';
import {CartLineItem} from '../components/CartLineItem';
import {EmptyState} from '../states/EmptyState';

/** Checkout sidebar: editable cart lines + subtotal. */
export function OrderSummary({storeContext}: { storeContext: StoreContext }) {
    const t = useTranslations('PAGE.CHECKOUT');
    const {cart, isEmpty, updateQuantity, removeProduct, status} = useCart(storeContext);
    return (
        <aside className="rounded-card border bg-card p-4 text-card-foreground lg:sticky lg:top-[calc(var(--header-h-lg)+1rem)]" aria-label={t('CART_DETAILS')}>
            <h2 className="text-lg font-semibold">{t('CART_DETAILS')}</h2>
            {isEmpty ? <EmptyState kind="cart"/> : (
                <>
                    <ul className="divide-y">
                        {cart!.products!.map(p => <CartLineItem key={p.id} product={p} onQuantity={updateQuantity} onRemove={removeProduct} busy={status === 'busy'}/>)}
                    </ul>
                    <Separator className="my-3"/>
                    <div className="flex items-center justify-between text-base font-medium">
                        <span>{t('SUB_TOTAL')}</span><span>{cart!.displaySubTotal}</span>
                    </div>
                </>
            )}
        </aside>
    );
}
