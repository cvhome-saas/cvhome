import {useCallback, useEffect, useState} from "react";
import {Cart} from "@/types/cart";
import {StoreContext} from "@/types/store-context";
import {getCartManager} from "@/services/cart-manager";
import {showToast} from "nextjs-toast-notify";
import {toastDirection} from "@/services/direction-utils";
import {useTranslations} from "next-intl";

export const useCart = (storeContext: StoreContext) => {
    const t = useTranslations('COMPONENTS.PRODUCT');
    const [cart, setCart] = useState<Cart | undefined>()
    const cartManager = getCartManager(storeContext);
    useEffect(cartManager.fullSubscribe(setCart), [cartManager]);

    const addToCart = useCallback(async (sku: string, quantity: number) => {
        cartManager.addProductToCart(sku, quantity, (cart) => {
            if (cart) {
                showToast.success(t('SUCCESSFULLY_ADDED_PRODUCT_TO_CART'), {
                    duration: 3000,
                    progress: false,
                    position: toastDirection(storeContext.locale),
                    transition: "bounceIn",
                    sound: false,
                });
            }
        }, () => {
            showToast.error(t('FAILED_TO_ADD_PRODUCT_TO_CART'), {
                duration: 3000,
                progress: false,
                position: toastDirection(storeContext.locale),
                transition: "bounceIn",
                sound: false,
            });
        });
    }, [cartManager, storeContext.locale, t]);

    const removeProduct = useCallback(async (productSku: string) => {
        cartManager.removeProduct(productSku, (cart) => {
            if (cart) {
                showToast.success(t('SUCCESSFULLY_REMOVED_PRODUCT'), {
                    duration: 3000,
                    progress: false,
                    position: toastDirection(storeContext.locale),
                    transition: "bounceIn",
                    sound: false,
                });
            }
        }, () => {
            showToast.error(t('FAILED_TO_REMOVED_PRODUCT'), {
                duration: 3000,
                progress: false,
                position: toastDirection(storeContext.locale),
                transition: "bounceIn",
                sound: false,
            });
        });
    }, [cartManager, storeContext.locale, t]);


    return {cart, setCart, cartManager, addToCart, removeProduct};
}
