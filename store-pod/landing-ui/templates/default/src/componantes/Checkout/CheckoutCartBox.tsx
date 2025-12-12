'use client'
import {CartProductList} from "@/componantes/Cart/CartProductList";
import {StoreContext} from "@/types/store-context";
import {useTranslations} from "next-intl";
import {Card, CardContent, CardFooter, CardHeader, CardTitle} from "@/components/ui/card";
import {ScrollArea} from "@/components/ui/scroll-area";
import {Separator} from "@/components/ui/separator";
import {useCart} from "@/hooks/use-cart";

export const CheckoutCartBox = ({storeContext}: { storeContext: StoreContext }) => {
    const t = useTranslations('PAGE.CHECKOUT');
    const {cart} = useCart(storeContext);

    return (
        <Card>
            <CardHeader>
                <CardTitle>{t('CART_DETAILS')}</CardTitle>
            </CardHeader>
            <CardContent>
                <ScrollArea className="h-96">
                    <CartProductList storeContext={storeContext} cart={cart}/>
                </ScrollArea>
            </CardContent>
            <CardFooter className="flex-col items-start">
                <Separator className="mb-4"/>
                {cart && <>
                    <div className="space-y-2 border-t pt-6">
                        <div className="flex justify-between text-base font-medium text-foreground">
                            <p>{t('SUB_TOTAL')}</p>
                            <p>{cart.displaySubTotal}</p>
                        </div>
                    </div>
                </>}
            </CardFooter>
        </Card>
    )
}