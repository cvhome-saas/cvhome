'use client'
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import type {PageContext} from '@store-front/theme';
import {useCart} from '@store-front/hooks/use-cart';
import {Button} from '@store-front/ui/button';
import {Drawer, DrawerClose, DrawerContent, DrawerDescription, DrawerHeader, DrawerTitle} from '@store-front/ui/drawer';
import {CartLineItem} from '../components/CartLineItem';
import {EmptyState} from '../states/EmptyState';

/** The order form: ruled lines, quantities, a subtotal in the price voice, one primary action. */
export function CartDrawer({ctx, open, onOpenChange}: { ctx: PageContext; open: boolean; onOpenChange: (o: boolean) => void }) {
    const t = useTranslations('COMPONENTS.CART');
    const {cart, isEmpty, count, updateQuantity, removeProduct, status} = useCart(ctx.storeContext);
    return (
        <Drawer open={open} onOpenChange={onOpenChange}>
            <DrawerContent side="end" className="flex w-full flex-col gap-0 p-0 sm:max-w-md">
                <DrawerHeader className="flex-row items-end justify-between gap-4 border-b">
                    <DrawerTitle className="display text-2xl">{t('CART_DETAILS')}</DrawerTitle>
                    <DrawerDescription className="text-sm tabular-nums text-muted-foreground">{t('ITEMS_COUNT', {count})}</DrawerDescription>
                </DrawerHeader>
                <div className="flex-1 overflow-y-auto">
                    {isEmpty ? (
                        <EmptyState kind="cart" action={<DrawerClose asChild><Button variant="outline">{t('CONTINUE_SHOPPING')}</Button></DrawerClose>}/>
                    ) : (
                        <ul className="divide-y">
                            {cart!.products!.map(p => (
                                <CartLineItem key={p.id} product={p} onQuantity={updateQuantity} onRemove={removeProduct} busy={status === 'busy'}/>
                            ))}
                        </ul>
                    )}
                </div>
                {!isEmpty && (
                    <div className="border-t p-4">
                        <div className="flex items-baseline justify-between gap-4">
                            <span className="text-sm font-semibold uppercase tracking-wide text-muted-foreground">{t('SUB_TOTAL')}</span>
                            <span className="price text-2xl">{cart!.displaySubTotal}</span>
                        </div>
                        <p className="mt-1 text-xs text-muted-foreground">{t('SHOPPING_AND_TAX_CALCULATION_MESSAGE')}</p>
                        <Button asChild size="lg" className="mt-4 h-12 w-full text-base font-semibold">
                            <Link prefetch={false} href="/checkout">{t('CHECKOUT')}</Link>
                        </Button>
                        <DrawerClose asChild>
                            <Button variant="link" className="mt-1 w-full">{t('CONTINUE_SHOPPING')}</Button>
                        </DrawerClose>
                    </div>
                )}
            </DrawerContent>
        </Drawer>
    );
}
