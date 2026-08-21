import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {ReadableProductGroup} from '@models/catalog';

const CATALOG_API_BASE = '/spg/catalog/api/v1';

/**
 * Ported from
 * seller-ui/projects/seller-core/catalog/src/lib/products/product-related/services/product-relationship.service.ts,
 * verified against `catalog-service/api/v1/product/ProductRelationshipApi.java`.
 *
 * "Customers also bought" — the set of products related to one product.
 *
 * The same `ProductGroupFacade` and the same `ReadableProductGroup` shape as product groups, which
 * is why the read answers a group rather than a list: a relationship *is* a group, one per product,
 * created implicitly. `products` on the answer is the related set.
 *
 * The read is **public** (`/products/{id}/relationship`, no `private`) while both writes are
 * private. That asymmetry is the controller's, not a slip here: the storefront needs to render the
 * related strip for an anonymous shopper.
 */
@Injectable({providedIn: 'root'})
export class ProductRelationshipService {
  private readonly crudService = inject(CrudService);

  related(productId: number): Observable<ReadableProductGroup> {
    return this.crudService.get(`${CATALOG_API_BASE}/products/${productId}/relationship`);
  }

  add(productId: number, relatedProductId: number): Observable<void> {
    return this.crudService.post(
      `${CATALOG_API_BASE}/private/products/${productId}/relationship/${relatedProductId}`,
      {},
    );
  }

  remove(productId: number, relatedProductId: number): Observable<void> {
    return this.crudService.delete(
      `${CATALOG_API_BASE}/private/products/${productId}/relationship/${relatedProductId}`,
    );
  }
}
