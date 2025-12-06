'use client'
import {useEffect, useState} from "react";
import {Cart} from "@/types/cart";
import {getCartData} from "@/services/cart-utils";
import {CartProductList} from "@/componantes/Cart/CartProductList";
import {StoreContext} from "@/types/store-context";
import {CartSummary} from "@/componantes/Cart/CartSummary";
import {emitter} from "next/client";
import {useTranslations} from "next-intl";
import {Card, CardContent, CardFooter, CardHeader, CardTitle} from "@/components/ui/card";
import {ScrollArea} from "@/components/ui/scroll-area";
import {Separator} from "@/components/ui/separator";

export const CheckoutCartBox = ({storeContext}: { storeContext: StoreContext }) => {
    const t = useTranslations('PAGE.CHECKOUT');
    const [cart, setCart] = useState<Cart | undefined>()

    useEffect(() => {
        const handleCartChange = () => {
            setCart(getCartData());
        };

        handleCartChange(); // Set initial cart data
        emitter.on("CART_STORAGE_CHANGED", handleCartChange);

        // Cleanup listener on component unmount
        return () => {
            emitter.off("CART_STORAGE_CHANGED", handleCartChange);
        };
    }, []);

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