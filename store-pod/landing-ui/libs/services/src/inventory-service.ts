import {Product, ProductVariant} from "@store-front/types/product-groups";
import {Store} from "@store-front/types/store";
import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {apiFetch, get, orUndefined, publicPost} from "./http-utils";
import {StoreService} from "./store-service";

/**
 * One sku's stock and price from `GET /inventory/api/v1/availability` — the inventory service, split
 * out of catalog. A sku with no inventory record is absent from the response, not present with zeros.
 */
export interface SkuInventory {
    sku: string;
    productId?: number | null;
    available: boolean;
    canBePurchased: boolean;
    quantity: number;
    quantityOrderMinimum?: number;
    quantityOrderMaximum?: number;
    /** Raw amounts; formatting is this side's job. Null when the sku has no price configured. */
    price?: {
        originalPrice: number;
        finalPrice: number;
        discounted: boolean;
        discountPercent: number;
    } | null;
}

/**
 * Since the catalog/inventory split, catalog product payloads carry no price, quantity or
 * purchasability — those live in the inventory service, keyed by sku. This module fetches them in
 * bulk and folds them back into the `Product` shape the themes and hooks already render, so the
 * split stays invisible above the service layer.
 */
export class InventoryService {

    /** The store record, for the currency — one fetch per store, shared by every enrichment. */
    private static storeBySite = new Map<string, Promise<Store | undefined>>();

    /** Above this many skus the GET's query string gets uncomfortable; the POST body form takes over. */
    private static readonly QUERY_BODY_THRESHOLD = 40;

    /** Degrades: a listing without prices is still a listing. */
    public static getBySkus = async (storeContext: StoreContext, skus: string[]): Promise<SkuInventory[] | undefined> => {
        if (skus.length === 0) return [];
        if (skus.length > InventoryService.QUERY_BODY_THRESHOLD) {
            // A PDP whose variant matrix exceeds what a query string comfortably carries — same
            // answer shape, the sku list travels as a JSON body.
            return orUndefined(apiFetch<SkuInventory[]>(
                `${storeBaseServiceUrl('inventory', storeContext)}/api/v1/availability/query?store=${storeContext.store}&lang=${storeContext.locale}`,
                publicPost({skus})));
        }
        return orUndefined(apiFetch<SkuInventory[]>(
            `${storeBaseServiceUrl('inventory', storeContext)}/api/v1/availability?skus=${encodeURIComponent(skus.join(','))}&store=${storeContext.store}&lang=${storeContext.locale}`,
            get()));
    }

    /** Fold stock and price into every product of the list, in place. Undefined lists pass through. */
    public static enrichProducts = async (storeContext: StoreContext, products: Product[] | undefined): Promise<void> => {
        if (!products || products.length === 0) return;
        const skus = products.map(p => p.sku).filter(Boolean);
        const [inventories, store] = await Promise.all([
            InventoryService.getBySkus(storeContext, skus),
            InventoryService.storeFor(storeContext),
        ]);
        const bySku = new Map((inventories ?? []).map(inv => [inv.sku, inv]));
        for (const product of products) {
            InventoryService.applyInventory(product, bySku.get(product.sku), storeContext, store);
        }
    }

    /**
     * The PDP enrichment: price and stock for the product's **whole variant matrix** in one call —
     * the default variant's sku plus every combination sku — attached per variant so the buy box
     * can swap price and stock as the shopper selects. Listings never come through here; a page of
     * cards is one sku per product through `enrichProducts`.
     */
    public static enrichProduct = async (storeContext: StoreContext, product: Product | undefined): Promise<void> => {
        if (!product) return;
        const variants = product.variants ?? [];
        const skus = [...new Set([product.sku, ...variants.map(v => v.sku)].filter(Boolean))];
        const [inventories, store] = await Promise.all([
            InventoryService.getBySkus(storeContext, skus),
            InventoryService.storeFor(storeContext),
        ]);
        const bySku = new Map((inventories ?? []).map(inv => [inv.sku, inv]));
        InventoryService.applyInventory(product, bySku.get(product.sku), storeContext, store);
        for (const variant of variants) {
            InventoryService.applyVariantInventory(variant, bySku.get(variant.sku), storeContext, store);
        }
    }

    private static storeFor(storeContext: StoreContext): Promise<Store | undefined> {
        let pending = InventoryService.storeBySite.get(storeContext.store);
        if (!pending) {
            pending = StoreService.getStore(storeContext).then(
                store => store,
                () => {
                    // A failed store read must not pin `undefined` forever — drop it so the next
                    // enrichment retries, and fall back to unformatted amounts meanwhile.
                    InventoryService.storeBySite.delete(storeContext.store);
                    return undefined;
                });
            InventoryService.storeBySite.set(storeContext.store, pending);
        }
        return pending;
    }

    /**
     * Reconstructs the pre-split embedded fields (`quantity`, `canBePurchased`, `productPrice`,
     * `finalPrice`, …) so `useProductPurchase` and every theme keep reading what they always read.
     * No inventory record means not stocked: quantity 0, not purchasable, no price shown.
     */
    private static applyInventory(product: Product, inventory: SkuInventory | undefined,
                                  storeContext: StoreContext, store: Store | undefined): void {
        if (!inventory) {
            product.quantity = 0;
            product.canBePurchased = false;
            return;
        }
        product.quantity = inventory.quantity;
        product.canBePurchased = inventory.canBePurchased;
        if (inventory.quantityOrderMinimum !== undefined) product.quantityOrderMinimum = inventory.quantityOrderMinimum;
        if (inventory.quantityOrderMaximum !== undefined) product.quantityOrderMaximum = inventory.quantityOrderMaximum;

        const price = inventory.price;
        if (!price) return;
        const fmt = (amount: number | undefined): string =>
            amount === undefined ? '' : InventoryService.formatAmount(amount, storeContext, store);
        const discounted = !!price.discounted;
        product.price = price.finalPrice ?? 0;
        product.finalPrice = fmt(price.finalPrice);
        product.originalPrice = fmt(price.originalPrice);
        product.discounted = discounted;
        product.productPrice = {
            id: 0,
            finalPrice: fmt(price.finalPrice),
            originalPrice: fmt(price.originalPrice),
            discounted,
            description: undefined as never,
        };
    }

    /**
     * The `VariantPricing` section — the ONLY writer of a variant's price/stock fields, mirroring
     * `applyInventory`'s contract for the product itself. No inventory record means not sellable.
     */
    private static applyVariantInventory(variant: ProductVariant, inventory: SkuInventory | undefined,
                                         storeContext: StoreContext, store: Store | undefined): void {
        if (!inventory) {
            variant.quantity = 0;
            variant.canBePurchased = false;
            return;
        }
        variant.quantity = inventory.quantity;
        variant.canBePurchased = inventory.canBePurchased;
        // The per-order bounds are per sku, so a combination may sell in different amounts than its
        // siblings. Dropping them here is what let the buy box offer a quantity the cart then refused.
        variant.quantityOrderMinimum = inventory.quantityOrderMinimum;
        variant.quantityOrderMaximum = inventory.quantityOrderMaximum;
        const price = inventory.price;
        if (!price) return;
        const fmt = (amount: number | undefined): string =>
            amount === undefined ? '' : InventoryService.formatAmount(amount, storeContext, store);
        variant.finalPrice = fmt(price.finalPrice);
        variant.originalPrice = fmt(price.originalPrice);
        variant.discounted = !!price.discounted;
    }

    /** Currency-formatted when the store record is at hand, a plain amount when it is not. */
    private static formatAmount(amount: number, storeContext: StoreContext, store: Store | undefined): string {
        if (store?.currency) {
            try {
                return new Intl.NumberFormat(storeContext.locale, {style: 'currency', currency: store.currency})
                    .format(amount);
            } catch {
                // an unknown currency code must not take the price down with it
            }
        }
        return amount.toFixed(2);
    }
}
