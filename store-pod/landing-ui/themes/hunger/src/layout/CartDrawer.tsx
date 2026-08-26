'use client'
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import type {PageContext} from '@store-front/theme';
import {useCart} from '@store-front/hooks/use-cart';
import {Drawer, DrawerClose, DrawerContent, DrawerDescription, DrawerHeader, DrawerTitle} from '@store-front/ui/drawer';
import {CartLineItem} from '../components/CartLineItem';
import {EmptyState} from '../states/EmptyState';

/** The order docket, torn off the pad: numbered lines, quantities, the total struck across the plate. */
export function CartDrawer({ctx, open, onOpenChange}: { ctx: PageContext; open: boolean; onOpenChange: (o: boolean) => void }) {
    const t = useTranslations('COMPONENTS.CART');
    const {cart, isEmpty, count, updateQuantity, removeProduct, status} = useCart(ctx.storeContext);
    return (
        <Drawer open={open} onOpenChange={onOpenChange}>
            <DrawerContent side="end" className="flex w-full flex-col border-s-2 border-foreground p-0 sm:max-w-md">
                <DrawerHeader className="plate">
                    <DrawerTitle className="press text-2xl">{t('CART_DETAILS')}</DrawerTitle>
                    <DrawerDescription className="text-primary-foreground/80">{t('ITEMS_COUNT', {count})}</DrawerDescription>
                </DrawerHeader>
                <div className="flex-1 overflow-y-auto px-4">
                    {isEmpty ? (
                        <EmptyState kind="cart" action={
                            <DrawerClose asChild>
                                <button type="button" className="fold h-10 px-5">{t('CONTINUE_SHOPPING')}</button>
                            </DrawerClose>
                        }/>
                    ) : (
                        <ul>
                            {cart!.products!.map(p => (
                                <CartLineItem key={p.id} product={p} onQuantity={updateQuantity} onRemove={removeProduct} busy={status === 'busy'}/>
                            ))}
                        </ul>
                    )}
                </div>
                {!isEmpty && (
                    <div className="border-t-2 border-foreground p-4">
                        <div className="flex items-baseline justify-between gap-4">
                            <span className="press text-lg">{t('SUB_TOTAL')}</span>
                            <span className="price text-2xl">{cart!.displaySubTotal}</span>
                        </div>
                        <p className="mt-1 text-xs text-muted-foreground">{t('SHOPPING_AND_TAX_CALCULATION_MESSAGE')}</p>
                        <Link prefetch={false} href="/checkout" className="fold plate mt-4 h-12 w-full justify-center border-primary text-base">
                            {t('CHECKOUT')}
                        </Link>
                        <DrawerClose asChild>
                            <button type="button" className="press mt-2 w-full py-2 text-xs tracking-wide text-muted-foreground hover:text-foreground">
                                {t('CONTINUE_SHOPPING')}
                            </button>
                        </DrawerClose>
                    </div>
                )}
            </DrawerContent>
        </Drawer>
    );
}
