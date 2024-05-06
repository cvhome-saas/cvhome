import React from "react";
import {CheckoutBox} from "@/componants/checkout/CheckoutBox";
import {RequestCookie} from "next/dist/compiled/@edge-runtime/cookies";
import {cookies, headers} from "next/headers";
import {Cart} from "@/types/cart";
import {CartService} from "@/services/cart-service";
import {getTranslations} from "next-intl/server";
import {extractStoreContext, StoreContext} from "@/types/store-context";
import {BreadCrumb} from "@/componants/product/sub-componants/BreadCrumb";
import {ContentService} from "@/services/content-service";


export default async function CheckoutPage({params}: { params: { locale: string } }) {
    const storeContext: StoreContext = extractStoreContext(headers(), cookies(), params.locale);

    const cookie: RequestCookie | undefined = cookies().get("store-ui-cart-id" as any)
    let cart: Cart | undefined;
    if (cookie) {
        cart = await CartService.getCart(storeContext, cookie.value);
    }
    const box = await ContentService.getBox(storeContext, 'agreement');
    const t = await getTranslations('Checkout');
    return <>
        <BreadCrumb name={t('Checkout')} t={{'Home': 'Home'}}/>
        <CheckoutBox storeContext={storeContext} agreement={box.description.description} cart={cart} t={{
            "Billing Details": t("Billing Details"),
            "First Name": t("First Name"),
            "Last Name": t("Last Name"),
            "Company Name": t("Company Name"),
            "Street Address": t("Street Address"),
            "House number and street name": t("House number and street name"),
            "Country": t("Country"),
            "Town/City": t("Town/City"),
            "State": t("State"),
            "Postcode": t("Postcode"),
            "Phone": t("Phone"),
            "Email address": t("Email address"),
            "Additional information": t("Additional information"),
            "Order notes": t("Order notes"),
            "Notes about your order, special notes for delivery": t("Notes about your order, special notes for delivery"),
            "I agree with the terms and conditions": t("I agree with the terms and conditions"),
            "Place your order": t("Place your order"),
            "Product": t("Product"),
            "Total": t("Total"),
            "No items found in checkout": t("No items found in checkout"),
            "Shop now": t("Shop now"),
        }}/>
    </>
}