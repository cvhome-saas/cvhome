import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {ReadableImage} from '@models/catalog';

const CATALOG_API_BASE = '/spg/catalog/api/v1';

/**
 * Ported from
 * seller-ui/projects/seller-core/catalog/src/lib/products/services/product-image.service.ts,
 * verified against `catalog-service/api/v1/product/ProductImageApi.java`.
 *
 * A product's images.
 *
 * **seller-core's `createImage` targets a mapping that does not exist.** It posts to the plural
 * form of the image path; `ProductImageApi` maps only the singular `…/product/{id}/image`. Every
 * image upload from the old console has 404'd. The port uses the singular path and drops the dead
 * method, as it drops the two `*Url()` helpers that only existed to hand a raw URL to a
 * third-party uploader.
 *
 * The multipart part is named `file` and the controller binds it as `MultipartFile[]`, so one part
 * per file and several may ride in one request. The console sends one at a time: an upload that
 * fails halfway through a batch leaves the operator unable to say which image is missing.
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
   * Upload one image.
   *
   * `order` is its position. `defaultImage` asks for the storefront thumbnail, and it is the **only**
   * moment at which that can be decided: `PATCH …/image/{imageId}` sets `sortOrder` and nothing
   * else, so no endpoint re-designates an existing image. Worse, `buildContentImages` sets the flag
   * on the new image without clearing it on the old one, so passing `true` for a product that
   * already has a default leaves two. The console therefore sends `defaultImage` only for the first
   * image on a product — where the pod would set it anyway — and shows which image is the thumbnail
   * without offering to change it. See lessons.md, "Catalogue — a product's default image cannot be
   * changed after upload".
   *
   * `HttpClient` sets the multipart boundary from the `FormData`; setting `Content-Type` by hand
   * here would break it.
   */
  upload(productId: number, file: File, order: number, defaultImage: boolean): Observable<void> {
    const body = new FormData();
    body.append('file', file, file.name);
    return this.crudService.post(
      `${CATALOG_API_BASE}/private/product/${productId}/image`,
      body,
      {order, defaultImage},
    );
  }

  /** Move an image to a position. The body is ignored; `?order=` is the whole request. */
  reorder(productId: number, imageId: number, order: number): Observable<void> {
    return this.crudService.patch(
      `${CATALOG_API_BASE}/private/product/${productId}/image/${imageId}`,
      {},
      {order},
    );
  }

  remove(productId: number, imageId: number): Observable<void> {
    return this.crudService.delete(
      `${CATALOG_API_BASE}/private/product/${productId}/image/${imageId}`,
    );
  }
}
