import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {PersistableInventory, ReadableInventory, SkuInventory} from '@models/catalog';

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
   * Create or update the SKU's single inventory record — quantity, availability and default price
   * in one write. This is the console's whole write path for stock and price.
   */
  upsert(sku: string, inventory: PersistableInventory): Observable<ReadableInventory> {
    return this.crudService.put(`${INVENTORY_V1}/private/inventory/${encodeURIComponent(sku)}`, inventory);
  }

  /**
   * Orphan cleanup after a product delete. Best-effort by design: the rows carry the catalog's
   * product id, and deleting a product whose inventory is already gone is a no-op.
   */
  deleteByProduct(productId: number): Observable<void> {
    return this.crudService.delete(`${INVENTORY_V1}/private/inventory/by-product/${productId}`);
  }
}
