'use client'

import {X} from "lucide-react";
import {Link} from "@/i18n/navigation";
import {StoreContext} from "@/types/store-context";
import {Cart} from "@/types/cart";
import {CartProductList} from "@/componantes/Cart/CartProductList";
import {CartSummary} from "@/componantes/Cart/CartSummary";
import {isRtl} from "@/services/direction-utils";
import {useTranslations} from "next-intl";
import {
    Sheet,
    SheetContent,
    SheetHeader,
    SheetTitle,
    SheetClose,
} from "@/components/ui/sheet";

export const NavCartDialog = (
    {
        storeContext,
        cart,
        setCart,
        cartOpen,
        setCartOpen
    }: {
        storeContext: StoreContext,
        cart: Cart | undefined,
        setCart: (cart: Cart | undefined) => void
        cartOpen: boolean,
        setCartOpen: (cart: boolean) => void
    },
) => {
    const t = useTranslations("COMPONENTS.CART");
    const isRtlLayout = isRtl(storeContext.locale);

    return (
        <Sheet open={cartOpen} onOpenChange={setCartOpen}>
            <SheetContent
                side={isRtlLayout ? "left" : "right"}
                className="flex h-full flex-col p-0 bg-background shadow-xl max-w-md w-full"
            >
                <div className="flex-1 overflow-y-auto px-4 py-6 sm:px-6">
                    <SheetHeader className="flex flex-row items-start justify-between space-y-0">
                        <SheetTitle className="text-lg font-medium text-foreground">
                            {t('CART_DETAILS')}
                        </SheetTitle>

                        <div className={`${isRtlLayout ? 'me-3' : 'ms-3'} flex h-7 items-center`}>
                            <SheetClose
                                type="button"
                                className="relative -m-2 p-2 text-neutral hover:text-hover-neutral rounded-md"
                            >
                                <span className="sr-only">Close panel</span>
                                <X aria-hidden="true" className="size-6"/>
                            </SheetClose>
                        </div>
                    </SheetHeader>

                    <div className="mt-8">
                        <CartProductList storeContext={storeContext} cart={cart} setCart={setCart}/>
                    </div>
                </div>

                <div className="border-t border-border px-4 py-6 sm:px-6">
                    {
                        cart && cart.products && cart.products.length > 0 && (
                            <>
                                <CartSummary cart={cart}/>
                                <p className="mt-0.5 text-sm text-neutral">
                                    {t('SHOPPING_AND_TAX_CALCULATION_MESSAGE')}
                                </p>
                                <div className="mt-6">
                                    <Link
                                        prefetch={false}
                                        href="/checkout"
                                        className="flex items-center justify-center rounded-md border border-transparent bg-primary px-6 py-3 text-base font-medium text-foreground shadow-xs"
                                    >
                                        {t('CHECKOUT')}
                                    </Link>
                                </div>
                            </>
                        )
                    }

                    <div className="mt-6 flex justify-center text-center text-sm text-neutral">
                        <p>
                            {t('OR')}{' '}
                            <SheetClose
                                type="button"
                                className="font-medium text-primary"
                            >
                                {t('CONTINUE_SHOPPING')}
                                <span
                                    aria-hidden="true"
                                    className="inline-block rtl:-scale-x-100"
                                >
                  {' '}
                                    &rarr;
                </span>
                            </SheetClose>
                        </p>
                    </div>
                </div>
            </SheetContent>
        </Sheet>
    );
}