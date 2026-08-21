import {Injectable, inject} from '@angular/core';
import {Observable, catchError, map, of, shareReplay} from 'rxjs';

import {ProductService} from '@api/catalog/product.service';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import type {RelatedProduct} from '@models/products';

/**
 * How many products the pickers hold to search within.
 *
 * A ceiling, not a page size — nothing pages through this. It has to be high enough that a store's
 * whole catalogue usually fits, and low enough that a picker does not become a bulk download; 500
 * is what the taxonomy lists already use for the same reason.
 */
const SEARCH_POOL = 500;

/**
 * Finding a product by typing part of its name.
 *
 * **The server cannot do this.** `ProductRepository.findAll` builds its `Specification` from store,
 * language, `available`, `sku`, `manufacturerId` and `categoryIds`, and reads no name field at all;
 * `ProductCriteria.productName` and `Criteria.name` are bound by Spring and consumed by nobody. So a
 * `name=` parameter is accepted, ignored, and answered with an unfiltered page.
 *
 * That is not a theoretical gap. seller-ui's product autocomplete passes `name=` to
 * `/api/v2/private/tiny-products` and has therefore **never actually searched** — it shows the first
 * twenty products in the store whatever you type, which looks like a working control until you
 * type the name of the twenty-first.
 *
 * Since it cannot be done on the server, it is done here, honestly: one bounded read per store, held
 * for the session, filtered on the client over **both** the name and the SKU. The bound is real and
 * the pickers say so in their hint rather than pretending the whole catalogue is reachable.
 */
@Injectable({providedIn: 'root'})
export class ProductSearch {
  private readonly products = inject(ProductService);
  private readonly shell = inject(ConsoleShellFacade);

  private readonly pools = new Map<string, Observable<readonly RelatedProduct[]>>();

  /** How many products the pool holds, so a consumer can say what it searched. */
  readonly poolSize = SEARCH_POOL;

  /**
   * Products whose name or SKU contains `term`.
   *
   * An empty term answers the head of the pool rather than nothing, which is what makes the picker
   * usable before anything has been typed — the operator can browse rather than having to guess a
   * fragment.
   */
  find(term: string, excludeId: number | null, limit = 10): Observable<readonly RelatedProduct[]> {
    const needle = normalise(term);
    return this.pool().pipe(
      map((all) => {
        const matches = all.filter(
          (product) =>
            product.id !== excludeId &&
            (needle === '' ||
              normalise(product.name).includes(needle) ||
              normalise(product.sku).includes(needle)),
        );
        return matches.slice(0, limit);
      }),
    );
  }

  /** Drops the cached pool, so a newly created product is findable without a reload. */
  invalidate(): void {
    this.pools.clear();
  }

  /**
   * The store's products, read once.
   *
   * `shareReplay(1)` per store id: the group picker and the related-products picker both search, and
   * without this each keystroke that missed the debounce would have re-read the catalogue.
   */
  private pool(): Observable<readonly RelatedProduct[]> {
    const store = this.shell.currentStoreId() ?? '';
    const existing = this.pools.get(store);
    if (existing) {
      return existing;
    }

    const pool = this.products.search({page: 0, count: SEARCH_POOL}).pipe(
      map((page) =>
        page.content.map(
          (product): RelatedProduct => ({
            id: product.id,
            name: product.description?.name ?? product.sku ?? String(product.id),
            sku: product.sku ?? '',
          }),
        ),
      ),
      // A picker that cannot reach the catalogue offers nothing; it does not break the page it is on.
      catchError(() => of<readonly RelatedProduct[]>([])),
      shareReplay({bufferSize: 1, refCount: false}),
    );
    this.pools.set(store, pool);
    return pool;
  }
}

/**
 * Case- and accent-insensitive, so "cafe" finds "Café".
 *
 * `NFD` splits a letter from its diacritic and the range strips the marks — the same normalisation
 * a shopper would expect from a storefront search, applied to the operator's side of the platform.
 */
function normalise(value: string): string {
  return value
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .trim()
    .toLowerCase();
}
