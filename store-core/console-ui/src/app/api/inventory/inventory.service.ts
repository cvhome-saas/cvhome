import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@cvhome-saas/ui-kit';
import type {
  PersistableInventory,
  PersistableSkuInventory,
  ReadableInventory,
  SkuInventory,
} from '@models/catalog';

const INVENTORY_V1 = '/spg/inventory/api/v1';

/**
 * Stock and price, from the inventory service.
 *
 * Split out of catalog: since the catalog/inventory split, product responses carry no price or
 * quantity — the console fetches them here, keyed by SKU, and merges them client-side. The write
 * side is a single sku-addressed upsert; the catalog product write no longer creates an
 * availability or a price implicitly.
 */
@Injectable({providedIn: 'root'})
export class InventoryService {
  private readonly crudService = inject(CrudService);

  /**
   * Stock and price for a set of SKUs in one call.
   *
   * SKUs with no inventory record are simply absent from the answer — a brand-new product that has
   * never been priced is "not stocked", not an error.
   */
  bySkus(skus: readonly string[]): Observable<readonly SkuInventory[]> {
    return this.crudService.get(`${INVENTORY_V1}/availability`, {skus: skus.join(',')});
  }

  /**
   * Every sku of the given products, in one call.
   *
   * The list needs this rather than `bySkus`: a product's stock is the sum of its variants', and the page only
   * knows each product's **default** sku — reporting that one row's quantity as the product's understates a
   * variant product's stock by everything the other combinations hold.
   */
  byProducts(productIds: readonly number[]): Observable<readonly SkuInventory[]> {
    return this.crudService.get(`${INVENTORY_V1}/private/inventory/by-products`, {
      productIds: productIds.join(','),
    });
  }

  /**
   * Create or update the SKU's single inventory record — quantity, availability and default price
   * in one write. This is the console's whole write path for stock and price.
   */
  upsert(sku: string, inventory: PersistableInventory): Observable<ReadableInventory> {
    return this.crudService.put(`${INVENTORY_V1}/private/inventory/${encodeURIComponent(sku)}`, inventory);
  }

  /**
   * A whole variant matrix's stock and prices in one write — `PUT /private/inventory/bulk`.
   *
   * One request rather than one round-trip per sku: a 20-variant matrix is one call, and the pod
   * caps the batch at 200 entries. The body is a record wrapping the list (`{entries: […]}`),
   * because Spring 6.1's method-parameter validation turns a naked `@RequestBody List` failure
   * into a 500 through the shared advice.
   */
  bulkUpsert(entries: readonly PersistableSkuInventory[]): Observable<readonly SkuInventory[]> {
    return this.crudService.put(`${INVENTORY_V1}/private/inventory/bulk`, {entries});
  }

  /**
   * Orphan cleanup after a product delete. Best-effort by design: the rows carry the catalog's
   * product id, and deleting a product whose inventory is already gone is a no-op.
   */
  deleteByProduct(productId: number): Observable<void> {
    return this.crudService.delete(`${INVENTORY_V1}/private/inventory/by-product/${productId}`);
  }

  /**
   * Retire one sku's inventory row — the cleanup after a variant-set replace removes a
   * combination. An in-flight reservation of the deleted sku still settles safely; its release
   * simply has no row left to restock.
   */
  deleteBySku(sku: string): Observable<void> {
    return this.crudService.delete(`${INVENTORY_V1}/private/inventory/${encodeURIComponent(sku)}`);
  }
}
