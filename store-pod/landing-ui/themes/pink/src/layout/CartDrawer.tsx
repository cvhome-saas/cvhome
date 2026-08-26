'use client'
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import type {PageContext} from '@store-front/theme';
import {useCart} from '@store-front/hooks/use-cart';
import {Button} from '@store-front/ui/button';
import {Drawer, DrawerClose, DrawerContent, DrawerDescription, DrawerHeader, DrawerTitle} from '@store-front/ui/drawer';
import {CartLineItem} from '../components/CartLineItem';
import {EmptyState} from '../states/EmptyState';

/** The order slip: a flooded head, the lines ruled under it, the subtotal printed on the biggest flag. */
export function CartDrawer({ctx, open, onOpenChange}: { ctx: PageContext; open: boolean; onOpenChange: (o: boolean) => void }) {
    const t = useTranslations('COMPONENTS.CART');
    const {cart, isEmpty, count, updateQuantity, removeProduct, status} = useCart(ctx.storeContext);
    return (
        <Drawer open={open} onOpenChange={onOpenChange}>
            <DrawerContent side="end" className="hair flex w-full flex-col rounded-none border-s-2 p-0 sm:max-w-md">
                <DrawerHeader className="flood tone-light hair border-b-2 p-4">
                    <DrawerTitle className="display text-2xl">{t('CART_DETAILS')}</DrawerTitle>
                    <DrawerDescription className="cover-line text-primary-foreground">{t('ITEMS_COUNT', {count})}</DrawerDescription>
                </DrawerHeader>
                <div className="ruled-stock flex-1 overflow-y-auto px-4">
                    {isEmpty ? (
                        <EmptyState kind="cart" action={<DrawerClose asChild><Button variant="outline">{t('CONTINUE_SHOPPING')}</Button></DrawerClose>}/>
                    ) : (
                        <ul>
                            {cart!.products!.map(p => (
                                <CartLineItem key={p.id} product={p} onQuantity={updateQuantity} onRemove={removeProduct} busy={status === 'busy'}/>
                            ))}
                        </ul>
                    )}
                </div>
                {!isEmpty && (
                    <div className="hair border-t-2 p-4">
                        <div className="flex items-center justify-between gap-3">
                            <span className="cover-line">{t('SUB_TOTAL')}</span>
                            <span className="flag flag-lg">{cart!.displaySubTotal}</span>
                        </div>
                        <p className="mt-2 text-xs text-muted-foreground">{t('SHOPPING_AND_TAX_CALCULATION_MESSAGE')}</p>
                        <Button asChild size="lg" className="mt-4 w-full">
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
