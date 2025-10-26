'use client'
import {Dispatch, SetStateAction} from 'react'
import {Dialog, DialogBackdrop, DialogPanel} from '@headlessui/react'
import {XMarkIcon} from '@heroicons/react/24/outline'
import {Product} from "@/types/product-groups";
import {ProductDetails} from "@/componantes/ProductDetails/ProductDetails";
import {StoreContext} from "@/types/store-context";
import {isRtl} from "@/services/direction-utils";
import {useTranslations} from "next-intl";

export default function ProductQuickVIew({storeContext, product, open, setOpen}: {
    storeContext: StoreContext,
    product: Product,
    open: boolean,
    setOpen: Dispatch<SetStateAction<boolean>>
}) {
    const t = useTranslations('PAGE.PRODUCT');
    const isRtlLayout = isRtl(storeContext.locale);

    return (
        <Dialog open={open} onClose={setOpen} className="relative z-10">
            <DialogBackdrop
                transition
                className="fixed inset-0 hidden bg-neutral/75 transition-opacity data-closed:opacity-0 data-enter:duration-300 data-enter:ease-out data-leave:duration-200 data-leave:ease-in md:block"/>

            <div className="fixed inset-0 z-10 w-screen overflow-y-auto">
                <div
                    className="flex min-h-full items-stretch justify-center  md:items-center md:px-2 lg:px-4">
                    <DialogPanel
                        transition
                        className="flex w-full transform  text-base transition data-closed:translate-y-4 data-closed:opacity-0 data-enter:duration-300 data-enter:ease-out data-leave:duration-200 data-leave:ease-in md:my-8 md:max-w-2xl md:px-4 data-closed:md:translate-y-0 data-closed:md:scale-95 lg:max-w-4xl">
                        <div
                            className="relative flex w-full items-center overflow-hidden bg-background px-4 pt-14 pb-8 shadow-2xl sm:px-6 sm:pt-8 md:p-6 lg:p-8">
                            <button
                                type="button"
                                onClick={() => setOpen(false)}
                                className={`absolute top-4 ${isRtlLayout ? 'left-4' : 'right-4'} text-neutral hover:text-foreground sm:top-8 ${isRtlLayout ? 'sm:left-6' : 'sm:right-6'} md:top-6 ${isRtlLayout ? 'md:left-6' : 'md:right-6'} lg:top-8 ${isRtlLayout ? 'lg:left-8' : 'lg:right-8'}`}
                            >
                                <span className="sr-only">Close</span>
                                <XMarkIcon aria-hidden="true" className="size-6"/>
                            </button>

                            <ProductDetails storeContext={storeContext} p={product} t={t}/>

                        </div>
                    </DialogPanel>
                </div>
            </div>
        </Dialog>
    );
}