import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@cvhome-saas/ui-kit';
import type {PersistableVariantSet, ReadableProductVariantDefinition} from '@models/catalog';

const CATALOG_API_BASE = '/spg/catalog/api/v2';

/**
 * A product's variant set — v2, beside the product definition it belongs to.
 *
 * One read and one write. The write is an **atomic whole-set replace**: the payload names the axes
 * (option codes from the store vocabulary, in display order) and the combinations together, so the
 * two can never disagree — a product with axes always has combinations by construction, which is
 * why no separate publishability endpoint exists. Combinations carrying their id keep their rows.
 * Empty options and variants restore the single default variant, and the product sells by one sku
 * again.
 *
 * Guardrails are the pod's: at most 4 options and 100 variants per product (422), duplicate skus
 * and duplicate combinations refused (409). Price and stock are not here — they are the inventory
 * service's, one row per variant sku, written through `InventoryService.bulkUpsert`.
 */
@Injectable({providedIn: 'root'})
export class ProductVariantService {
  private readonly crudService = inject(CrudService);

  /** The matrix rows, each with resolved option/value labels in the console's active language. */
  list(productId: number): Observable<readonly ReadableProductVariantDefinition[]> {
    return this.crudService.get(`${CATALOG_API_BASE}/private/product/${productId}/variants`);
  }

  /** Answers `void`. The caller re-reads rather than assuming the write took. */
  replace(productId: number, set: PersistableVariantSet): Observable<void> {
    return this.crudService.put(`${CATALOG_API_BASE}/private/product/${productId}/variants`, set);
  }
}
