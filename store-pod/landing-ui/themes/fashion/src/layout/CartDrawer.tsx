'use client'
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import type {PageContext} from '@store-front/theme';
import {useCart} from '@store-front/hooks/use-cart';
import {Drawer, DrawerClose, DrawerContent, DrawerDescription, DrawerHeader, DrawerTitle} from '@store-front/ui/drawer';
import {CartLineItem} from '../components/CartLineItem';
import {EmptyState} from '../states/EmptyState';

/** The bag: a stretch of wall from the reading-end side; lines are small sheets, the total a sheet, checkout a day-glo strip. */
export function CartDrawer({ctx, open, onOpenChange}: { ctx: PageContext; open: boolean; onOpenChange: (o: boolean) => void }) {
    const t = useTranslations('COMPONENTS.CART');
    const {cart, isEmpty, count, updateQuantity, removeProduct, status} = useCart(ctx.storeContext);
    return (
        <Drawer open={open} onOpenChange={onOpenChange}>
            <DrawerContent side="end" className="flex w-full flex-col border-0 bg-(--wall) p-0 [background-image:var(--grain)] sm:max-w-md">
                <DrawerHeader className="flex-row items-center gap-3 pb-2">
                    <DrawerTitle className="strip h-9 text-base [--tilt:-1deg]">{t('CART_DETAILS')}</DrawerTitle>
                    <DrawerDescription className="strip h-7 bg-foreground px-2 text-xs text-background [--tilt:1deg]">{t('ITEMS_COUNT', {count})}</DrawerDescription>
                </DrawerHeader>
                <div className="flex-1 overflow-y-auto px-4 py-2">
                    {isEmpty ? (
                        <EmptyState kind="cart" action={<DrawerClose asChild><button type="button" className="strip strip-hover h-10">{t('CONTINUE_SHOPPING')}</button></DrawerClose>}/>
                    ) : (
                        <ul className="wall wall-calm flex flex-col gap-3">
                            {cart!.products!.map(p => (
                                <CartLineItem key={p.id} product={p} onQuantity={updateQuantity} onRemove={removeProduct} busy={status === 'busy'}/>
                            ))}
                        </ul>
                    )}
                </div>
                {!isEmpty && (
                    <div className="p-4">
                        <div className="sheet [--tilt:0.4deg]">
                            <div className="flex items-center justify-between px-4 pt-4 font-display text-lg uppercase">
                                <span>{t('SUB_TOTAL')}</span>
                                <span className="tabular-nums">{cart!.displaySubTotal}</span>
                            </div>
                            <p className="px-4 pb-3 pt-1 text-xs text-muted-foreground">{t('SHOPPING_AND_TAX_CALCULATION_MESSAGE')}</p>
                            <Link prefetch={false} href="/checkout" className="glo h-12 w-full text-base">{t('CHECKOUT')}</Link>
                        </div>
                        <DrawerClose asChild>
                            <button type="button" className="mt-3 w-full py-2 text-center text-sm underline underline-offset-4 hover:decoration-primary hover:decoration-2">{t('CONTINUE_SHOPPING')}</button>
                        </DrawerClose>
                    </div>
                )}
            </DrawerContent>
        </Drawer>
    );
}
