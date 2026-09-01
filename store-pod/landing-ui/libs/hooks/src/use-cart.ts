'use client'
import {useCallback, useEffect, useMemo, useState} from "react";
import {Cart} from "@store-front/types/cart";
import {getCartManager} from "@store-front/services/cart-manager";
import {useTranslations} from "next-intl";
import {StoreContext} from "@store-front/types/store-context";
import {notify} from "./notify";
import {useErrorMessage} from "./use-error-message";

export type CartStatus = 'idle' | 'busy';

export const useCart = (storeContext: StoreContext) => {
    const t = useTranslations('COMPONENTS.PRODUCT');
    const messageFor = useErrorMessage();
    const [cart, setCart] = useState<Cart | undefined>();
    const [status, setStatus] = useState<CartStatus>('idle');
    const cartManager = useMemo(() => getCartManager(storeContext), [storeContext.store, storeContext.locale]);
    useEffect(cartManager.fullSubscribe(setCart), [cartManager]);
    const locale = storeContext.locale;

    const addToCart = useCallback(async (sku: string, quantity: number) => {
        setStatus('busy');
        cartManager.addProductToCart(sku, quantity, (c) => {
            setStatus('idle');
            if (c) notify('success', t('SUCCESSFULLY_ADDED_PRODUCT_TO_CART'), locale);
        }, (error) => {
            setStatus('idle');
            notify('error', messageFor(error, t('FAILED_TO_ADD_PRODUCT_TO_CART')), locale);
        });
    }, [cartManager, locale, messageFor, t]);

    const updateQuantity = useCallback(async (sku: string, quantity: number) => {
        setStatus('busy');
        cartManager.updateQuantity(sku, quantity, () => setStatus('idle'), (error) => {
            setStatus('idle');
            notify('error', messageFor(error, t('FAILED_TO_UPDATE_QUANTITY')), locale);
        });
    }, [cartManager, locale, messageFor, t]);

    const removeProduct = useCallback(async (productSku: string) => {
        setStatus('busy');
        cartManager.removeProduct(productSku, (c) => {
            setStatus('idle');
            if (c) notify('success', t('SUCCESSFULLY_REMOVED_PRODUCT'), locale);
        }, (error) => {
            setStatus('idle');
            notify('error', messageFor(error, t('FAILED_TO_REMOVED_PRODUCT')), locale);
        });
    }, [cartManager, locale, messageFor, t]);

    const count = cart?.quantity ?? 0;
    const isEmpty = !cart || !cart.products || cart.products.length === 0;

    return {cart, setCart, cartManager, addToCart, updateQuantity, removeProduct, status, count, isEmpty};
}
