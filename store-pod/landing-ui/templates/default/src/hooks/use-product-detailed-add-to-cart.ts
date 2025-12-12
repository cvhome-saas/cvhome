import {useCallback, useEffect, useState} from "react";
import {Product} from "@/types/product-groups";
import {StoreContext} from "@/types/store-context";
import {showToast} from "nextjs-toast-notify";
import {toastDirection} from "@/services/direction-utils";
import {useTranslations} from "next-intl";
import {getCartManager} from "@/services/cart-manager";

export const useProductDetailedAddToCart = (storeContext: StoreContext, product: Product) => {
    const t = useTranslations('PAGE.PRODUCT');
    const [quantity, setQuantity] = useState(product.quantity > 0 ? 1 : 0);
    const [isInCart, setIsInCart] = useState(false);
    const cartManager = getCartManager(storeContext);


    const syncWithCart = useCallback(() => {
        const matchedProduct = cartManager.getMatchedProductsInCart(product.id);
        if (matchedProduct) {
            setQuantity(matchedProduct.quantity);
            setIsInCart(true);
        } else {
            setQuantity(product.quantity > 0 ? 1 : 0);
            setIsInCart(false);
        }
    }, [cartManager, product.id, product.quantity]);

    useEffect(cartManager.fullSubscribe(syncWithCart), [cartManager]);

    const updateCart = useCallback(async (newQty: number) => {
        cartManager.addProductToCart(product.sku, newQty, (cart) => {
            if (cart) {
                setIsInCart(true);
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
    }, [cartManager, storeContext.locale, product.sku, t]);

    const incrementQuantity = useCallback(async () => {
        const newQty = quantity + 1;
        if (product.quantity < newQty) return;

        setQuantity(newQty);
        if (isInCart) {
            await updateCart(newQty);
        }
    }, [quantity, product.quantity, isInCart, updateCart]);

    const decrementQuantity = useCallback(async () => {
        const newQty = quantity - 1;
        if (newQty < 1) return;

        setQuantity(newQty);
        if (isInCart) {
            await updateCart(newQty);
        }
    }, [quantity, isInCart, updateCart]);

    const addToCart = useCallback(async () => {
        if (!isInCart) {
            await updateCart(quantity);
        }
    }, [isInCart, quantity, updateCart]);

    return {
        quantity,
        incrementQuantity,
        decrementQuantity,
        addToCart,
        isInCart,
    };
};