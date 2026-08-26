'use client'
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import type {PageContext} from '@store-front/theme';
import {useCart} from '@store-front/hooks/use-cart';
import {Button} from '@store-front/ui/button';
import {Drawer, DrawerClose, DrawerContent, DrawerDescription, DrawerHeader, DrawerTitle} from '@store-front/ui/drawer';
import {CartLineItem} from '../components/CartLineItem';
import {Figure} from '../components/Figure';
import {EmptyState} from '../states/EmptyState';

/** The delivery docket: an enamel head, ruled lines, and the running total in its own tabular slot. */
export function CartDrawer({ctx, open, onOpenChange}: { ctx: PageContext; open: boolean; onOpenChange: (o: boolean) => void }) {
    const t = useTranslations('COMPONENTS.CART');
    const {cart, isEmpty, count, updateQuantity, removeProduct, status} = useCart(ctx.storeContext);
    return (
        <Drawer open={open} onOpenChange={onOpenChange}>
            <DrawerContent side="end" className="flex w-full flex-col p-0 sm:max-w-md">
                <DrawerHeader className="enamel-flat rounded-none px-5 py-4">
                    <DrawerTitle className="sign-lg text-lg">{t('CART_DETAILS')}</DrawerTitle>
                    <DrawerDescription className="text-sm opacity-85">{t('ITEMS_COUNT', {count})}</DrawerDescription>
                </DrawerHeader>
                <div className="flex-1 overflow-y-auto px-5">
                    {isEmpty ? (
                        <EmptyState kind="cart" action={
                            <DrawerClose asChild>
                                <Button variant="outline" className="sign rule-brass text-[0.625rem]">{t('CONTINUE_SHOPPING')}</Button>
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
                    <div className="rule-brass border-t bg-background p-5">
                        <div className="flex items-baseline justify-between">
                            <span className="sign text-[0.6875rem]">{t('SUB_TOTAL')}</span>
                            <Figure value={cart!.displaySubTotal ?? ''} className="text-xl"/>
                        </div>
                        <p className="mt-1.5 text-xs text-muted-foreground">{t('SHOPPING_AND_TAX_CALCULATION_MESSAGE')}</p>
                        <Button asChild size="lg" className="sign mt-5 w-full text-[0.6875rem]">
                            <Link prefetch={false} href="/checkout">{t('CHECKOUT')}</Link>
                        </Button>
                        <DrawerClose asChild>
                            <Button variant="link" className="sign mt-1 w-full text-[0.625rem] text-muted-foreground">{t('CONTINUE_SHOPPING')}</Button>
                        </DrawerClose>
                    </div>
                )}
            </DrawerContent>
        </Drawer>
    );
}
