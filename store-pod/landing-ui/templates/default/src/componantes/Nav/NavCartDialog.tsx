'use client'
import {Dialog, DialogBackdrop, DialogPanel, DialogTitle} from "@headlessui/react";
import {XMarkIcon} from "@heroicons/react/24/outline";
import {Link} from "@/i18n/navigation";
import {StoreContext} from "@/types/store-context";
import {Cart} from "@/types/cart";
import {CartProductList} from "@/componantes/Cart/CartProductList";
import {CartSummary} from "@/componantes/Cart/CartSummary";
import {isRtl} from "@/services/direction-utils";
import {useTranslations} from "next-intl";

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
        <Dialog open={cartOpen} onClose={setCartOpen} className="relative z-10">
            <DialogBackdrop
                transition
                className="fixed inset-0 bg-neutral/75 transition-opacity duration-500 ease-in-out data-closed:opacity-0"
            />

            <div className="fixed inset-0 overflow-hidden">
                <div className="absolute inset-0 overflow-hidden">
                    <div
                        className={`pointer-events-none fixed inset-y-0 ${isRtlLayout ? 'left-0' : 'right-0'} flex max-w-full ${isRtlLayout ? 'pr-10' : 'pl-10'}`}>
                        <DialogPanel
                            transition
                            className={`pointer-events-auto w-screen max-w-md transform transition duration-500 ease-in-out ${isRtlLayout ? 'data-closed:-translate-x-full' : 'data-closed:translate-x-full'} sm:duration-700`}
                        >
                            <div className="flex h-full flex-col overflow-y-scroll bg-background shadow-xl">
                                <div className="flex-1 overflow-y-auto px-4 py-6 sm:px-6">
                                    <div className="flex items-start justify-between">
                                        <DialogTitle className="text-lg font-medium text-foreground">
                                            {t('CART_DETAILS')}
                                        </DialogTitle>
                                        <div className={`${isRtlLayout ? 'me-3' : 'ms-3'} flex h-7 items-center`}>
                                            <button
                                                type="button"
                                                onClick={() => setCartOpen(false)}
                                                className="relative -m-2 p-2 text-neutral hover:text-hover-neutral"
                                            >
                                                <span className="absolute -inset-0.5"/>
                                                <span className="sr-only">Close panel</span>
                                                <XMarkIcon aria-hidden="true" className="size-6"/>
                                            </button>
                                        </div>
                                    </div>

                                    <div className="mt-8">
                                        <CartProductList storeContext={storeContext} cart={cart} setCart={setCart}/>
                                    </div>
                                </div>

                                <div className="border-t border-border px-4 py-6 sm:px-6">
                                    {
                                        cart && cart.products && cart.products.length > 0 && <>
                                            <CartSummary cart={cart}/>
                                            <p className="mt-0.5 text-sm text-neutral">
                                                {t('SHOPPING_AND_TAX_CALCULATION_MESSAGE')}
                                            </p>
                                            <div className="mt-6">
                                                <Link prefetch={false} href="/checkout"
                                                      className="flex items-center justify-center rounded-md border border-transparent bg-primary px-6 py-3 text-base font-medium text-foreground shadow-xs ">
                                                    {t('CHECKOUT')}
                                                </Link>
                                            </div>
                                        </>
                                    }

                                    <div className="mt-6 flex justify-center text-center text-sm text-neutral">
                                        <p>
                                            {t('OR')}{' '}
                                            <button
                                                type="button"
                                                onClick={() => setCartOpen(false)}
                                                className="font-medium text-primary"
                                            >
                                                {t('CONTINUE_SHOPPING')}
                                                <span aria-hidden="true"
                                                      className="inline-block rtl:-scale-x-100"> &rarr;</span>
                                            </button>
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </DialogPanel>
                    </div>
                </div>
            </div>
        </Dialog>
    );
}