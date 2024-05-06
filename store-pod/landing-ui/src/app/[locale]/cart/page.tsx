import {BreadCrumb} from "@/componants/product/sub-componants/BreadCrumb";
import React, {Fragment} from "react";
import {cookies, headers} from "next/headers";
import {Cart} from "@/types/cart";
import {extractStoreContext, StoreContext} from "@/types/store-context";
import {FullCartBox} from "@/componants/product/sub-componants/FullCartBox";
import {useTranslations} from "next-intl";

export default function CartPage({params}: { params: { locale: string } }) {
    const storeContext: StoreContext = extractStoreContext(headers(), cookies(), params.locale);
    const t = useTranslations('Cart');
    return <>
        <BreadCrumb name={t('Cart')} t={{'Home': 'Home'}}/>
        <FullCartBox storeContext={storeContext} t={{
            'Your cart items': t('Your cart items'),
            'Image': t('Image'),
            'Product Name': t('Product Name'),
            'Unit Price': t('Unit Price'),
            'Qty': t('Qty'),
            'Subtotal': t('Subtotal'),
            'Action': t('Action'),
            'Clear Shopping Cart': t('Clear Shopping Cart'),
            'Continue Shopping': t('Continue Shopping'),
            'Cart Total': t('Cart Total'),
            'Total products': t('Total products'),
            'Grand Total': t('Grand Total'),
            'Proceed to Checkout': t('Proceed to Checkout'),
            'No items found in cart': t('No items found in cart'),
            'Shop now': t('Shop now'),
        }}/>
    </>
}