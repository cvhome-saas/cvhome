/** Ported from seller-ui/projects/seller-core/catalog/src/lib/products/services/product.service.ts. */
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {PageRequest, PageT} from '@core/table/table.types';
import type {
  CreatedEntity,
  EntityExists,
  LightPersistableProduct,
  PersistableProductDefinition,
  ReadableProduct,
  ReadableProductDefinition,
} from '@models/catalog';

/**
 * The filters `ProductCriteria` accepts, of those the console exposes.
 *
 * `categoryIds` is a `List<Long>` on the Java side. It is sent as a single value here because the
 * console's category filter is single-select — Spring binds one repetition of the parameter to a
 * one-element list, and `CrudService`'s parameter map holds scalars.
 */
export interface ProductQuery extends PageRequest {
  /** Substring match — `ProductRepository` builds a `LIKE %sku%`, not an equality. */
  readonly sku?: string;
  readonly available?: boolean;
  readonly categoryIds?: number;
  readonly manufacturerId?: number;
}

/**
 * What the two pickers narrow on.
 *
 * `sku`, not a name: `ProductCriteria.productName` exists and **is never read** — see the note on
 * `list` below. The SKU filter is the only text search the catalogue actually has.
 */
export interface TinyProductQuery extends PageRequest {
  readonly sku?: string;
}

const CATALOG_V1 = '/spg/catalog/api/v1';
const CATALOG_V2 = '/spg/catalog/api/v2';

/**
 * Ported from seller-ui/projects/seller-core/catalog/src/lib/products/services/product.service.ts,
 * verified method by method against `catalog-service/api/v1/product/ProductApi.java` and
 * `api/v2/product/ProductApiV2.java`.
 *
 * Products. The version split is real and is not tidiable: **v2 owns the product definition**
 * (create, read, update, and the two list endpoints) while **v1 owns everything else** (the inline
 * `PATCH`, delete, the uniqueness check and category membership). Both are current; v2 is not a
 * migration in progress.
 *
 * **Two of seller-core's calls have never worked**, both fixed in this port:
 *
 * 1. `addProductToCategory` appended a stray closing brace to the end of its category path, so the
 *    URL matched no mapping and every attempt 404'd. Fixed here, which is what makes the product
 *    form's Organize step possible at all.
 * 2. `createImage` posted to the **plural** image path. `ProductImageApi` maps only the singular
 *    one. That finding lives in `product-image.service.ts`; the dead method is not ported.
 *
 * **`PATCH /api/v1/private/product/{id}` is mapped twice** on `ProductApi` — once taking a
 * `LightPersistableProduct` body and once taking `?order=`, the two differing only by `produces`.
 * The console calls the body form; the `?order=` form is product ordering, which seller-ui has
 * commented out of its own menu and which is not ported. See lessons.md.
 */
@Injectable({providedIn: 'root'})
export class ProductService {
  private readonly crudService = inject(CrudService);

  /**
   * A page of products for the table.
   *
   * **`/products`, not `/private/base-products`, and this needs explaining.** seller-core calls the
   * private one and so did this port until it was run against the stack: `ReadableBaseProductMapper`
   * populates neither the description, nor the categories, nor the manufacturer, nor the image. A row
   * from it has an id, a SKU, a price, a quantity and a flag — no **name**. A product list that shows
   * only SKUs is not a product list, and no second endpoint fills the gap: `tiny-products` is thinner
   * still.
   *
   * `/products` runs the *identical query*. `ProductFacadeV2Impl.getProductListsByCriteria` and
   * `getBaseProductListsByCriteria` both delegate to the same `listProducts(...)` with the same
   * `productService.findAll(criteria, store)`; the only difference is which mapper is passed. So this
   * hides nothing a seller should see — in particular it does **not** filter out unpublished
   * products, which was the thing worth checking before making this swap.
   *
   * What it costs: `/products` carries no `@PreAuthorize`, so the console reads its own catalogue
   * through an unauthenticated endpoint. It is still store-scoped by the `store` parameter, and the
   * data is the storefront's own public catalogue, but it is a real asymmetry. See lessons.md,
   * "Catalogue — the private product list is stripped of everything a list needs".
   *
   * Note `count`, not `size` — `ServletWebConfig` renames Spring's page-size parameter platform-wide.
   */
  list(query: ProductQuery): Observable<PageT<ReadableProduct>> {
    return this.crudService.get(`${CATALOG_V2}/products`, {...query});
  }

  /**
   * The same list, one page deep, behind the two pickers.
   *
   * `/products` again rather than `tiny-products`, for the same reason and more sharply: a picker
   * whose results have no names is unusable, and `tiny-products` answers `description: null` on
   * every row.
   *
   * **It narrows on the SKU.** `ProductCriteria.productName` is accepted by the binder and
   * `getProductName()` is never called anywhere in the pod — verified by reading
   * `ProductRepository`'s predicate builder, which reads `sku`, `manufacturerId`, `categoryIds`,
   * `available` and nothing else. Sending a name filter returns the whole catalogue, silently. See
   * lessons.md, "Catalogue — the product-name filter is accepted and ignored".
   */
  search(query: TinyProductQuery): Observable<PageT<ReadableProduct>> {
    return this.crudService.get(`${CATALOG_V2}/products`, {...query});
  }

  /** The editable product, as the form's four steps read it. */
  definition(id: number): Observable<ReadableProductDefinition> {
    return this.crudService.get(`${CATALOG_V2}/private/product/${id}`);
  }

  /** Echoes an `Entity` — the new id and nothing else, which is what Save draft routes on. */
  create(product: PersistableProductDefinition): Observable<CreatedEntity> {
    return this.crudService.post(`${CATALOG_V2}/private/product`, product);
  }

  /** Answers `void`. The form reloads the definition rather than assuming the write took. */
  update(id: number, product: PersistableProductDefinition): Observable<void> {
    return this.crudService.put(`${CATALOG_V2}/private/product/${id}`, product);
  }

  /**
   * The table's inline edit: price, quantity and availability in one call.
   *
   * Every field on `LightPersistableProduct` is a Java primitive, so an omitted field is not "leave
   * it alone" — it is `false` or `0`. The caller therefore always sends all four, filled from the
   * row it is editing.
   */
  patch(id: number, product: LightPersistableProduct): Observable<void> {
    return this.crudService.patch(`${CATALOG_V1}/private/product/${id}`, product);
  }

  delete(id: number): Observable<void> {
    return this.crudService.delete(`${CATALOG_V1}/private/product/${id}`);
  }

  /** Whether a SKU is taken. Answers that and only that — it cannot suggest a free one. */
  skuTaken(code: string): Observable<EntityExists> {
    return this.crudService.get(`${CATALOG_V1}/private/product/unique`, {code});
  }

  /**
   * Put a product in a category.
   *
   * **The path seller-core has always got wrong.** Its version appended a stray `}`, so this has
   * never once succeeded from the old console. The Organize step diffs the selected set against
   * what the definition returned and calls this for each addition.
   */
  addToCategory(productId: number, categoryId: number): Observable<void> {
    return this.crudService.post(`${CATALOG_V1}/private/product/${productId}/category/${categoryId}`, {});
  }

  removeFromCategory(productId: number, categoryId: number): Observable<void> {
    return this.crudService.delete(`${CATALOG_V1}/private/product/${productId}/category/${categoryId}`);
  }
}
