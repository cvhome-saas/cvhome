'use client'
import {useEffect, useState} from "react";
import {Cart} from "@/types/cart";
import {CartProductList} from "@/componantes/Cart/CartProductList";
import {StoreContext} from "@/types/store-context";
import {CartSummary} from "@/componantes/Cart/CartSummary";
import {useTranslations} from "next-intl";
import {Card, CardContent, CardFooter, CardHeader, CardTitle} from "@/components/ui/card";
import {ScrollArea} from "@/components/ui/scroll-area";
import {Separator} from "@/components/ui/separator";
import {getCartManager} from "@/componantes/CartManager";

export const CheckoutCartBox = ({storeContext}: { storeContext: StoreContext }) => {
    const t = useTranslations('PAGE.CHECKOUT');
    const [cart, setCart] = useState<Cart | undefined>()
    const cartManager = getCartManager(storeContext);
    useEffect(cartManager.fullSubscribe(setCart), [cartManager]);

    return (
        <Card>
            <CardHeader>
                <CardTitle>{t('CART_DETAILS')}</CardTitle>
            </CardHeader>
            <CardContent>
                <ScrollArea className="h-96">
                    <CartProductList storeContext={storeContext} cart={cart} setCart={setCart}/>
                </ScrollArea>
            </CardContent>
            <CardFooter className="flex-col items-start">
                <Separator className="mb-4"/>
                <CartSummary cart={cart}/>
            </CardFooter>
        </Card>
    )
}