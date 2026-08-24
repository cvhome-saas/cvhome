'use client'
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import type {PageContext} from '@store-front/theme';
import {useCart} from '@store-front/hooks/use-cart';
import {Button} from '@store-front/ui/button';
import {Drawer, DrawerClose, DrawerContent, DrawerDescription, DrawerHeader, DrawerTitle} from '@store-front/ui/drawer';
import {cn} from '@store-front/ui/lib/utils';
import {CartLineItem} from '../components/CartLineItem';
import {EmptyState} from '../states/EmptyState';

/** How many load segments the meter draws; it caps there, it never lies about a threshold. */
const LOAD_SEGMENTS = 12;

/**
 * The basket: header sticker with the count, the load meter (one segment fills per item, honest and
 * capped — no invented free-shipping threshold), the crate lines, and the counter at the bottom with the
 * subtotal in the price voice.
 */
export function CartDrawer({ctx, open, onOpenChange}: { ctx: PageContext; open: boolean; onOpenChange: (o: boolean) => void }) {
    const t = useTranslations('COMPONENTS.CART');
    const {cart, isEmpty, count, updateQuantity, removeProduct, status} = useCart(ctx.storeContext);
    const filled = Math.min(count, LOAD_SEGMENTS);
    return (
        <Drawer open={open} onOpenChange={onOpenChange}>
            <DrawerContent side="end" className="flex w-full flex-col border-s-2 p-0 sm:max-w-md">
                <DrawerHeader className="border-b-2 pb-3">
                    <div className="flex items-center justify-between gap-3 pe-8">
                        <DrawerTitle className="signage text-2xl">{t('CART_DETAILS')}</DrawerTitle>
                        <span key={count} className={cn('sticker', count > 0 ? 'stamp-in bg-primary text-primary-foreground' : 'sticker-outline')}>
                            {t('ITEMS_COUNT', {count})}
                        </span>
                    </div>
                    <DrawerDescription className="sr-only">{t('ITEMS_COUNT', {count})}</DrawerDescription>
                    {/* the load meter */}
                    <div aria-hidden className="mt-2 grid grid-cols-12 gap-1">
                        {Array.from({length: LOAD_SEGMENTS}).map((_, i) => (
                            <span key={i} className={cn('h-1.5 rounded-badge transition-colors duration-(--motion-base) ease-standard',
                                i < filled ? 'bg-primary' : 'bg-muted')}/>
                        ))}
                    </div>
                </DrawerHeader>
                <div className="flex-1 overflow-y-auto px-4">
                    {isEmpty ? (
                        <EmptyState kind="cart" action={<DrawerClose asChild><Button variant="outline">{t('CONTINUE_SHOPPING')}</Button></DrawerClose>}/>
                    ) : (
                        <ul className="divide-y-2">
                            {cart!.products!.map(p => (
                                <CartLineItem key={p.id} product={p} onQuantity={updateQuantity} onRemove={removeProduct} busy={status === 'busy'}/>
                            ))}
                        </ul>
                    )}
                </div>
                {!isEmpty && (
                    <div className="border-t-2 bg-card p-4">
                        <div className="flex items-end justify-between gap-3">
                            <span className="signage text-lg">{t('SUB_TOTAL')}</span>
                            <span className="price text-3xl">{cart!.displaySubTotal}</span>
                        </div>
                        <p className="mt-1 text-xs text-muted-foreground">{t('SHOPPING_AND_TAX_CALCULATION_MESSAGE')}</p>
                        <Button asChild size="lg" className="signage mt-4 w-full text-lg">
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
