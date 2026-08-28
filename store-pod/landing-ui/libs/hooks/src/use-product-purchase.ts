'use client'
import {useCallback, useEffect, useMemo, useState} from "react";
import {useTranslations} from "next-intl";
import {Image, Product, ProductOption, ProductOptionValue, ProductVariant, StoreContext} from "@store-front/types";
import {getCartManager} from "@store-front/services/cart-manager";
import {sortedImages} from "@store-front/services/product-presenter";
import {notify} from "./notify";

export type PurchaseStatus = 'idle' | 'adding';

/** Options whose values pick a sellable variant, in display order. */
export const variantOptions = (p: Product): ProductOption[] =>
    (p.options ?? []).filter(o => o.variant && o.optionValues && o.optionValues.length > 0);

function variantMatches(v: ProductVariant, selection: Record<number, number>): boolean {
    const pairs: [number | undefined, number | undefined][] = [
        [v.variation?.option?.id, v.variation?.optionValue?.id],
        [v.variationValue?.option?.id, v.variationValue?.optionValue?.id],
    ];
    const defined = pairs.filter(([o, val]) => o !== undefined && val !== undefined) as [number, number][];
    if (defined.length === 0) return false;
    return defined.every(([o, val]) => selection[o] === val);
}

/**
 * Product purchase state: option selection → resolved variant (sku / price / images / stock) → quantity →
 * add to cart. Replaces `useProductDetailedAddToCart`. Stock, purchasability and the price arrive merged
 * into the product payload by the service layer (the inventory service owns them since the
 * catalog/inventory split); variant pricing is resolved from the payload itself.
 */
export const useProductPurchase = (storeContext: StoreContext, product: Product) => {
    const t = useTranslations('PAGE.PRODUCT');
    const cartManager = useMemo(() => getCartManager(storeContext), [storeContext.store, storeContext.locale]);
    const options = useMemo(() => variantOptions(product), [product]);
    const variants = product.variants ?? [];

    const [selection, setSelection] = useState<Record<number, number>>(() => {
        const initial: Record<number, number> = {};
        const def = variants.find(v => v.defaultSelection);
        if (def) {
            for (const vv of [def.variation, def.variationValue]) {
                if (vv?.option?.id !== undefined && vv.optionValue?.id !== undefined) initial[vv.option.id] = vv.optionValue.id;
            }
        }
        return initial;
    });
    const [quantity, setQuantity] = useState(1);
    const [status, setStatus] = useState<PurchaseStatus>('idle');

    const allSelected = options.every(o => selection[o.id] !== undefined);
    const variant = useMemo(() => allSelected && variants.length ? variants.find(v => variantMatches(v, selection)) : undefined, [allSelected, variants, selection]);

    const inventory = variant?.inventory?.[0];
    const sku = inventory?.sku ?? variant?.sku ?? product.sku;
    const maxQty = inventory?.quantity ?? product.quantity ?? 0;
    const isOutOfStock = !product.available || !product.canBePurchased || maxQty < 1 || (variant ? !variant.available : false);
    const images: Image[] = variant?.images?.length ? variant.images : sortedImages(product);

    const selectedValuePrice = useMemo(() => {
        // a single option with its own price (e.g. "Large +$2") when there is no sellable variant
        if (variant) return undefined;
        for (const o of options) {
            const v = o.optionValues.find(ov => ov.id === selection[o.id]);
            if (v?.price) return v.price;
        }
        return undefined;
    }, [options, selection, variant]);

    const finalPrice = inventory?.price ?? selectedValuePrice ?? product.productPrice?.finalPrice ?? product.finalPrice;
    const originalPrice = product.productPrice?.originalPrice ?? product.originalPrice;
    const discounted = !!product.productPrice?.discounted && !inventory?.price && !selectedValuePrice;

    useEffect(() => {
        // clamp quantity to what is sellable for the current selection
        setQuantity(q => Math.min(Math.max(1, q), Math.max(1, maxQty)));
    }, [maxQty]);

    const select = useCallback((optionId: number, valueId: number) => {
        setSelection(prev => ({...prev, [optionId]: valueId}));
    }, []);

    const isValueAvailable = useCallback((option: ProductOption, value: ProductOptionValue): boolean => {
        if (variants.length === 0) return true;
        const trial = {...selection, [option.id]: value.id};
        return variants.some(v => {
            const pairs = [v.variation, v.variationValue];
            return pairs.some(p => p?.option?.id === option.id && p.optionValue?.id === value.id)
                && Object.entries(trial).every(([o, val]) => {
                    const oid = Number(o);
                    if (oid === option.id) return true;
                    return pairs.some(p => p?.option?.id === oid && p.optionValue?.id === val) || !pairs.some(p => p?.option?.id === oid);
                });
        });
    }, [variants, selection]);

    const inCartQuantity = cartManager.getMatchedProductsInCart(product.id)?.quantity ?? 0;
    const canAdd = !isOutOfStock && allSelected && status === 'idle';

    const addToCart = useCallback(async () => {
        if (!canAdd) return;
        setStatus('adding');
        cartManager.addProductToCart(sku, quantity, (cart) => {
            setStatus('idle');
            if (cart) notify('success', t('SUCCESSFULLY_ADDED_PRODUCT_TO_CART'), storeContext.locale);
        }, () => {
            setStatus('idle');
            notify('error', t('FAILED_TO_ADD_PRODUCT_TO_CART'), storeContext.locale);
        });
    }, [canAdd, cartManager, sku, quantity, storeContext.locale, t]);

    return {
        options,
        selection,
        select,
        isValueAvailable,
        allSelected,
        variant,
        sku,
        images,
        price: {finalPrice, originalPrice, discounted},
        quantity,
        maxQty,
        canDecrease: quantity > 1,
        canIncrease: quantity < maxQty,
        incrementQuantity: () => setQuantity(q => Math.min(q + 1, Math.max(1, maxQty))),
        decrementQuantity: () => setQuantity(q => Math.max(1, q - 1)),
        isOutOfStock,
        inCartQuantity,
        canAdd,
        status,
        addToCart,
    };
};
