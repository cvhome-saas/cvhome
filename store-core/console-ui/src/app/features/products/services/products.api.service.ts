import {Injectable, inject} from '@angular/core';
import {Observable, catchError, forkJoin, map, of, switchMap} from 'rxjs';

import {CatalogReference} from '@api/catalog/catalog-reference.service';
import {ProductService, type ProductQuery} from '@api/catalog/product.service';
import {InventoryService} from '@api/inventory/inventory.service';
import type {PageRequest} from '@cvhome-saas/ui-kit';
import type {ReadableCategory, ReadableProduct, SkuInventory} from '@models/catalog';
import type {
  InlineProductEdit,
  ProductFilterOption,
  ProductFilters,
  ProductRow,
  ProductTab,
  ProductsSnapshot,
} from '@models/products';

/** What the page is asking for: a tab, some filters and a page. */
export interface ProductsQuery {
  readonly tab: ProductTab;
  readonly filters: ProductFilters;
  readonly page: PageRequest;
}

/** Two non-breaking spaces per level of nesting — an `option` will not take a CSS indent. */
const NBSP = '\u00a0\u00a0';

/**
 * The product list's data, and the one write it makes.
 *
 * Three legs, and only one of them is the page. The **list** is; if it fails the page failed. The
 * category and brand lists are the two filter selects, and a select that cannot be populated should
 * cost the operator that filter, not the table they were looking at.
 *
 * The store is a fourth, read only for its currency — prices arrive as bare numbers and there is no
 * per-product currency anywhere on this platform. Losing it means prices render as plain numbers,
 * which is worse than showing a symbol and better than showing the wrong one.
 */
@Injectable({providedIn: 'root'})
export class ProductsApi {
  private readonly products = inject(ProductService);
  private readonly inventory = inject(InventoryService);
  private readonly reference = inject(CatalogReference);

  /** What the last response said each row held, so an inline edit can send the fields it is not changing. */
  private readonly loadedRows = new Map<number, ReadableProduct>();

  loadSnapshot(query: ProductsQuery): Observable<ProductsSnapshot> {
    return forkJoin({
      /*
       * Price and quantity live in the inventory service since the catalog/inventory split; the
       * catalog rows carry neither. ONE bulk call for the page — **by product id, not by sku**: a
       * product's stock is the sum of its variants', and the listing payload only carries each
       * product's default sku, so a sku-keyed read reported the default variant's quantity as
       * though it were the product's and understated every variant product. A page whose inventory
       * read fails still renders — with empty price/stock cells, not an error.
       */
      pageWithStock: this.products.list(toProductQuery(query)).pipe(
        switchMap((page) => {
          const productIds = page.content.map((product) => product.id);
          const stock: Observable<readonly SkuInventory[]> = productIds.length
            ? this.inventory.byProducts(productIds).pipe(catchError(() => of<readonly SkuInventory[]>([])))
            : of<readonly SkuInventory[]>([]);
          return stock.pipe(map((inventories) => ({page, inventories})));
        }),
      ),
      /*
       * The shared reference cache, like the product form. These three do not change while an
       * operator pages through a table, and the list and the form ask for exactly the same ones —
       * so navigating list → product → list re-read all of them, three times, for data that had not
       * moved. The catalogue drops the cache after every write it makes.
       */
      categories: this.optional(this.reference.hierarchy()),
      brands: this.optional(this.reference.brandList()),
      currency: this.reference.store().pipe(
        map((store) => store.currency ?? null),
        catchError(() => of(null)),
      ),
    }).pipe(
      map(({pageWithStock, categories, brands, currency}) => {
        const {page, inventories} = pageWithStock;
        const bySku = new Map(inventories.map((inventory) => [inventory.sku, inventory]));
        // Every row of a product, so the stock cell can total them. Keyed by product id, which the
        // inventory rows carry precisely so this question can be asked.
        const byProduct = new Map<number, SkuInventory[]>();
        for (const inventory of inventories) {
          if (inventory.productId === null || inventory.productId === undefined) {
            continue;
          }
          const rows = byProduct.get(inventory.productId);
          if (rows) {
            rows.push(inventory);
          } else {
            byProduct.set(inventory.productId, [inventory]);
          }
        }
        this.loadedRows.clear();
        for (const product of page.content) {
          this.loadedRows.set(product.id, product);
        }
        return {
          page: {
            ...page,
            content: page.content.map((product) =>
              toRow(product, bySku.get(product.sku ?? ''), byProduct.get(product.id) ?? [])),
          },
          categories: flattenForSelect(categories?.content ?? [], 0),
          brands: (brands?.content ?? []).map((brand) => ({
            id: brand.id,
            // `descriptions` is null on the wire for a manufacturer — see `@models/catalog`.
            label: brand.description?.name ?? brand.descriptions?.[0]?.name ?? brand.code,
          })),
          currency,
        };
      }),
    );
  }

  /**
   * One row's price, quantity and availability.
   *
   * Two writes since the catalog/inventory split: the catalog `PATCH` carries the visibility flags
   * (both, because every field on `LightPersistableProduct` is a Java primitive and an omitted one
   * would become `false`), and the inventory upsert carries price and quantity. They run together;
   * either failing fails the edit, and the reload afterwards shows what actually landed.
   */
  applyInlineEdit(edit: InlineProductEdit, query: ProductsQuery): Observable<ProductsSnapshot> {
    const loaded = this.loadedRows.get(edit.id);
    const sku = loaded?.sku ?? '';
    return forkJoin([
      this.products.patch(edit.id, {
        available: edit.available,
        productShipeable: loaded?.productShipeable ?? true,
      }),
      this.inventory.upsert(sku, {
        productId: edit.id,
        quantity: edit.quantity,
        available: edit.available,
        price: {amount: edit.price ?? 0},
      }),
    ]).pipe(switchMap(() => this.loadSnapshot(query)));
  }

  delete(id: number, query: ProductsQuery): Observable<ProductsSnapshot> {
    return this.products.delete(id).pipe(
      /*
       * Orphan cleanup in the inventory service, best-effort: the product is gone either way, and
       * an inventory row without a product is invisible to every reader.
       */
      switchMap(() => this.inventory.deleteByProduct(id).pipe(catchError(() => of(null)))),
      switchMap(() => this.loadSnapshot(query)),
    );
  }

  private optional<T>(source: Observable<T>): Observable<T | null> {
    return source.pipe(catchError(() => of(null)));
  }
}

/* --------------------------------------------------------------------------- shaping ---- */

/**
 * The page's state, as `ProductCriteria`'s parameters.
 *
 * Every one of these is backed, and `categoryIds` and `manufacturerId` were wired into seller-ui's
 * facade and never reached a template — the console is the first to expose them.
 *
 * **`productName` is not sent**, because it is not read: the predicate builder in
 * `ProductRepository` covers `sku`, `manufacturerId`, `categoryIds` and `available` and nothing
 * else, so a name filter narrows nothing while appearing to. See lessons.md.
 *
 * `categoryIds` also resolves the branch: `listProducts` expands a single category id to its whole
 * lineage, so filtering by a parent includes everything underneath it.
 *
 * An empty string is omitted rather than sent: `sku=""` is a `LIKE %%`, which matches everything but
 * still costs the predicate.
 */
function toProductQuery(query: ProductsQuery): ProductQuery {
  const {sku, categoryId, brandId} = query.filters;
  return {
    page: query.page.page,
    count: query.page.count,
    ...(query.tab === 'all' ? {} : {available: query.tab === 'available'}),
    ...(sku.trim() ? {sku: sku.trim()} : {}),
    ...(categoryId !== null ? {categoryIds: categoryId} : {}),
    ...(brandId !== null ? {manufacturerId: brandId} : {}),
  };
}

/**
 * One product, as a table row.
 *
 * `description` is the single language the request asked for, which is the console's active
 * language — the request context stamps `lang`. A product with no copy in that language falls back
 * to its SKU rather than to an empty cell, because a nameless row is a row that cannot be acted on.
 */
function toRow(
  product: ReadableProduct,
  inventory: SkuInventory | undefined,
  allVariants: readonly SkuInventory[],
): ProductRow {
  return {
    id: product.id,
    name: product.description?.name ?? product.sku ?? String(product.id),
    sku: product.sku ?? '',
    // Every product owns ≥1 variant under the uniform model; a row that predates it reads as one.
    variantCount: product.variantCount ?? 1,
    categories: (product.categories ?? [])
      .map((category) => category.description?.name ?? category.descriptions[0]?.name ?? category.code)
      .filter(Boolean),
    brand:
      product.manufacturer?.description?.name ??
      product.manufacturer?.descriptions?.[0]?.name ??
      product.manufacturer?.code ??
      null,
    /*
     * The **default** variant's price, which is the merchant's own choice of what this product costs
     * on a card — deliberately not a range, and not the cheapest variant.
     */
    price: inventory?.price?.finalPrice ?? null,
    /*
     * The stock the merchant actually holds: every variant's quantity summed. Showing the default
     * variant's alone said "39 in stock" for a product with 48 across three combinations.
     */
    quantity: allVariants.length
      ? allVariants.reduce((total, row) => total + (row.quantity ?? 0), 0)
      : (inventory?.quantity ?? 0),
    available: product.available ?? false,
    shipeable: product.productShipeable ?? true,
    /*
     * The pod's own path, which is not necessarily a URL this browser can reach — the same gap the
     * store logo and the invoice hit in Modules 4 and 5. The row falls back to a glyph rather than
     * a broken-image icon.
     */
    imageUrl: product.image?.imageUrl ?? product.images?.[0]?.imageUrl ?? null,
  };
}

/**
 * The category tree as a flat list of options, indented so the shape survives a `<select>`.
 *
 * A `<select>` cannot nest, and a filter list of forty categories with no hierarchy is a filter
 * list nobody can find anything in. Non-breaking spaces rather than a CSS indent, because option
 * elements do not take one.
 */
function flattenForSelect(
  categories: readonly ReadableCategory[],
  depth: number,
): readonly ProductFilterOption[] {
  return categories.flatMap((category) => [
    {
      id: category.id,
      label:
        NBSP.repeat(depth) +
        (category.description?.name ?? category.descriptions[0]?.name ?? category.code),
    },
    ...flattenForSelect(category.children, depth + 1),
  ]);
}
