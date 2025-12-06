'use client'
import {Dispatch, SetStateAction} from 'react'
import {Product} from "@/types/product-groups";
import {ProductDetails} from "@/componantes/ProductDetails/ProductDetails";
import {StoreContext} from "@/types/store-context";
import {useTranslations} from "next-intl";
import {Dialog, DialogContent} from "@/components/ui/dialog";

export default function ProductQuickVIew({storeContext, product, open, setOpen}: {
    storeContext: StoreContext,
    product: Product,
    open: boolean,
    setOpen: Dispatch<SetStateAction<boolean>>
}) {
    const t = useTranslations('PAGE.PRODUCT');

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogContent
                className="max-w-2xl lg:max-w-4xl p-0"
                onOpenAutoFocus={(e) => e.preventDefault()} // Prevents auto-focusing on the first element
            >
                {/* The close button is now part of DialogContent by default */}
                <div className="p-6 lg:p-8">
                    <ProductDetails storeContext={storeContext} p={product} t={t}/>
                </div>
            </DialogContent>
        </Dialog>
    );
}