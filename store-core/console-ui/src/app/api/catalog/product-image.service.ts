import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {PersistableProductImage, ReadableImage} from '@models/catalog';

const CATALOG_API_BASE = '/spg/catalog/api/v1';

/**
 * Verified against `catalog-service/api/v1/ProductImageApi.java`.
 *
 * A product's gallery — references into the content service's media library.
 *
 * **Catalog does not accept uploads.** The bytes go to the media library, where they are
 * deduplicated, measured, given alt text and protected from deletion while something still shows
 * them; catalog stores the asset ids. That is what closed two holes this comment used to describe:
 * a product's default image could be seen but not changed, and an image could be deleted from under
 * a live listing with nothing noticing.
 */
@Injectable({providedIn: 'root'})
export class ProductImageService {
  private readonly crudService = inject(CrudService);

  /**
   * Every image on a product, in the order the pod holds them.
   *
   * A **public** read — `/product/{id}/images`, no `private` — because the storefront gallery uses
   * the same endpoint. Verified against the mapping; not a missing segment.
   */
  images(productId: number): Observable<ReadableImage[]> {
    return this.crudService.get(`${CATALOG_API_BASE}/product/${productId}/images`);
  }

  /**
   * Appends images after the ones the product already has.
   *
   * An asset id that is not this store's is refused with `CATALOG.PRODUCT_IMAGE.ASSET_UNKNOWN` and
   * nothing is written.
   */
  attach(productId: number, items: readonly PersistableProductImage[]): Observable<ReadableImage[]> {
    return this.crudService.post(`${CATALOG_API_BASE}/private/product/${productId}/images`, items);
  }

  /**
   * Replaces the whole gallery: order is the list order, and the item flagged default wins.
   *
   * Sent whole because a reorder that arrives one move at a time leaves gaps and ties that the
   * storefront resolves arbitrarily. This is also how the default image is changed.
   */
  replace(productId: number, items: readonly PersistableProductImage[]): Observable<ReadableImage[]> {
    return this.crudService.put(`${CATALOG_API_BASE}/private/product/${productId}/images`, items);
  }

  /** Detaches an image. The asset stays in the library, where other products may still use it. */
  remove(productId: number, imageId: number): Observable<void> {
    return this.crudService.delete(
      `${CATALOG_API_BASE}/private/product/${productId}/image/${imageId}`,
    );
  }
}
