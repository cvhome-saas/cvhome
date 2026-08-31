'use client'
import {useCallback, useEffect, useMemo, useState} from "react";
import {useSearchParams} from "next/navigation";
import {useTranslations} from "next-intl";
import {Image, Product, ProductOption, ProductOptionValue, ProductVariant, StoreContext} from "@store-front/types";
import {getCartManager} from "@store-front/services/cart-manager";
import {sortedImages} from "@store-front/services/product-presenter";
import {notify} from "./notify";

export type PurchaseStatus = 'idle' | 'adding';

/** The axes this product varies by, in display order. Every assigned option is variant-defining now. */
export const variantOptions = (p: Product): ProductOption[] =>
    (p.options ?? []).filter(o => o.values && o.values.length > 0);

/** Whether a variant is exactly the given selection — one value per option, set equality. */
const variantMatches = (v: ProductVariant, options: ProductOption[], selection: Record<number, number>): boolean =>
    options.length > 0
    && options.every(o => selection[o.id] !== undefined)
    && v.optionValueIds.length === options.length
    && options.every(o => v.optionValueIds.includes(selection[o.id]));

/** The selection a variant's value ids describe, via the options' own valueId → optionId index. */
function selectionOf(v: ProductVariant, options: ProductOption[]): Record<number, number> {
    const optionByValue = new Map<number, number>();
    for (const o of options) {
        for (const value of o.values) optionByValue.set(value.id, o.id);
    }
    const selection: Record<number, number> = {};
    for (const valueId of v.optionValueIds) {
        const optionId = optionByValue.get(valueId);
        if (optionId !== undefined) selection[optionId] = valueId;
    }
    return selection;
}

/**
 * Product purchase state under the uniform variant model: option selection → resolved variant (sku /
 * price / stock) → quantity → add to cart.
 *
 * Every product owns ≥1 variant; a product with no options resolves its single default variant with
 * nothing to select, and a multi-option product resolves once every axis has a value — both flow into
 * the same add-to-cart-by-sku call. Price and stock arrive attached per variant by
 * `InventoryService.enrichProduct` (the PDP fetches the whole matrix's availability in one call);
 * chips for combinations that do not exist, or whose sku inventory says cannot be bought, grey out.
 *
 * The resolved variant syncs to the URL as `?sku=` (history.replaceState — no reload, no server
 * re-render), so every combination is directly addressable and a shared link lands preselected.
 */
export const useProductPurchase = (storeContext: StoreContext, product: Product) => {
    const t = useTranslations('PAGE.PRODUCT');
    const searchParams = useSearchParams();
    const cartManager = useMemo(() => getCartManager(storeContext), [storeContext.store, storeContext.locale]);
    const options = useMemo(() => variantOptions(product), [product]);
    const variants = useMemo(() => product.variants ?? [], [product]);

    const [selection, setSelection] = useState<Record<number, number>>(() => {
        // `?sku=` wins — a shared variant link lands preselected; the default variant otherwise.
        const requested = searchParams?.get('sku');
        const preselected =
            (requested && variants.find(v => v.sku === requested))
            || variants.find(v => v.defaultVariant)
            || variants[0];
        return preselected ? selectionOf(preselected, options) : {};
    });
    const [quantity, setQuantity] = useState(1);
    const [status, setStatus] = useState<PurchaseStatus>('idle');

    const allSelected = options.every(o => selection[o.id] !== undefined);

    /**
     * The resolved combination. A product with no options resolves its single (default) variant
     * immediately — there is nothing to select and `allSelected` is vacuously true.
     */
    const variant = useMemo(() => {
        if (options.length === 0) return variants.find(v => v.defaultVariant) ?? variants[0];
        return allSelected ? variants.find(v => variantMatches(v, options, selection)) : undefined;
    }, [allSelected, variants, options, selection]);

    const sku = variant?.sku ?? product.sku;
    const maxQty = variant?.quantity ?? product.quantity ?? 0;
    const purchasable = variant ? variant.canBePurchased !== false : product.canBePurchased;
    const isOutOfStock = !product.available || !purchasable || maxQty < 1;
    // Per-variant images are a later phase; the gallery is the product's, whatever is selected.
    const images: Image[] = sortedImages(product);

    const finalPrice = variant?.finalPrice ?? product.productPrice?.finalPrice ?? product.finalPrice;
    const originalPrice = variant?.originalPrice ?? product.productPrice?.originalPrice ?? product.originalPrice;
    const discounted = variant ? !!variant.discounted : !!product.productPrice?.discounted;

    useEffect(() => {
        // clamp quantity to what is sellable for the current selection
        setQuantity(q => Math.min(Math.max(1, q), Math.max(1, maxQty)));
    }, [maxQty]);

    useEffect(() => {
        // The resolved variant is part of the page's address. Native history update, like the
        // listing's filters: Next syncs useSearchParams() with it and does not re-render the server
        // page. Only a real combination writes; nothing is erased while a selection is incomplete.
        if (typeof window === 'undefined' || options.length === 0 || !variant) return;
        const url = new URL(window.location.href);
        if (url.searchParams.get('sku') === variant.sku) return;
        url.searchParams.set('sku', variant.sku);
        window.history.replaceState(window.history.state, '', url.toString());
    }, [variant, options.length]);

    const select = useCallback((optionId: number, valueId: number) => {
        setSelection(prev => ({...prev, [optionId]: valueId}));
    }, []);

    /**
     * Whether picking this value (keeping the rest of the selection) can reach a sellable variant.
     * Two gates, both from data already in hand: the combination must exist, and its sku's
     * inventory must say it can be bought — sellability is inventory's one flag, merged in by
     * enrichment. Chips failing either grey out.
     */
    const isValueAvailable = useCallback((option: ProductOption, value: ProductOptionValue): boolean => {
        if (variants.length === 0) return true;
        return variants.some(v =>
            v.optionValueIds.includes(value.id)
            && v.canBePurchased !== false
            && Object.entries(selection).every(([optionId, valueId]) =>
                Number(optionId) === option.id || v.optionValueIds.includes(valueId)),
        );
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
