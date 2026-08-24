import {Product} from "@store-front/types/product-groups";
import {Store} from "@store-front/types/store";
import {storeBaseServiceUrl, StoreContext} from "@store-front/types/store-context";
import {apiFetch, get, orUndefined} from "./http-utils";
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

    /** Degrades: a listing without prices is still a listing. */
    public static getBySkus = async (storeContext: StoreContext, skus: string[]): Promise<SkuInventory[] | undefined> => {
        if (skus.length === 0) return [];
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

    public static enrichProduct = async (storeContext: StoreContext, product: Product | undefined): Promise<void> => {
        if (!product) return;
        await InventoryService.enrichProducts(storeContext, [product]);
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
