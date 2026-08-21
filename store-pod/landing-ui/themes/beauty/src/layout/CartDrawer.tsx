'use client'
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import type {PageContext} from '@store-front/theme';
import {useCart} from '@store-front/hooks/use-cart';
import {Drawer, DrawerClose, DrawerContent, DrawerDescription, DrawerHeader, DrawerTitle} from '@store-front/ui/drawer';
import {CartLineItem} from '../components/CartLineItem';
import {TagButton} from '../components/TagButton';
import {EmptyState} from '../states/EmptyState';

/** The bin: an ink-ruled panel from the reading-end side, lines as rows, the checkout tag at the foot. */
export function CartDrawer({ctx, open, onOpenChange}: { ctx: PageContext; open: boolean; onOpenChange: (o: boolean) => void }) {
    const t = useTranslations('COMPONENTS.CART');
    const {cart, isEmpty, count, updateQuantity, removeProduct, status} = useCart(ctx.storeContext);
    return (
        <Drawer open={open} onOpenChange={onOpenChange}>
            <DrawerContent side="end" className="flex w-full flex-col gap-0 rounded-none border-foreground bg-background p-0 shadow-overlay sm:max-w-md [&>button]:end-4 [&>button]:top-3.5 [&>button]:rounded-none">
                <DrawerHeader className="border-b border-foreground py-3">
                    <DrawerTitle className="q font-display text-base font-semibold uppercase tracking-wide">{t('CART_DETAILS')}</DrawerTitle>
                    <DrawerDescription className="font-mono text-xs uppercase tracking-wide text-muted-foreground">{t('ITEMS_COUNT', {count})}</DrawerDescription>
                </DrawerHeader>
                <div className="flex-1 overflow-y-auto px-4">
                    {isEmpty ? (
                        <EmptyState kind="cart" action={<DrawerClose asChild><TagButton size="sm">{t('CONTINUE_SHOPPING')}</TagButton></DrawerClose>}/>
                    ) : (
                        <ul className="divide-y divide-foreground">
                            {cart!.products!.map(p => <CartLineItem key={p.id} product={p} onQuantity={updateQuantity} onRemove={removeProduct} busy={status === 'busy'}/>)}
                        </ul>
                    )}
                </div>
                {!isEmpty && (
                    <div className="border-t border-foreground">
                        <div className="hazard-soft h-2" aria-hidden/>
                        <div className="flex flex-col gap-3 p-4">
                            <div className="flex items-baseline justify-between font-mono uppercase tracking-wide">
                                <span className="text-xs text-muted-foreground">{t('SUB_TOTAL')}</span>
                                <span className="text-lg font-bold">{cart!.displaySubTotal}</span>
                            </div>
                            <p className="font-mono text-[0.7rem] uppercase tracking-wide text-muted-foreground">{t('SHOPPING_AND_TAX_CALCULATION_MESSAGE')}</p>
                            <TagButton asChild size="lg" className="w-full"><Link prefetch={false} href="/checkout">{t('CHECKOUT')}</Link></TagButton>
                            <DrawerClose asChild><button type="button" className="q self-center font-mono text-xs uppercase tracking-wide underline underline-offset-4">{t('CONTINUE_SHOPPING')}</button></DrawerClose>
                        </div>
                    </div>
                )}
            </DrawerContent>
        </Drawer>
    );
}
