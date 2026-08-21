import {Observable, of} from 'rxjs';

import {ProductSearch} from '@features/product-form/services/product-search';
import type {RelatedProduct} from '@models/products';

/** Combining diacritics, so "cafe" matches "Café" here the way it does in the real search. */
const MARKS = /[\u0300-\u036f]/g;

/**
 * A product search with a fixed catalogue behind it.
 *
 * `ProductSearch` reads through `ProductService` to `HttpClient`, which no page spec provides, and
 * the pickers that use it are incidental to almost every test that trips over it. This keeps the
 * filtering **real** — it is the filtering, not the fetching, that the feature is — so a spec can
 * still assert that typing a name finds a product.
 */
export class FakeProductSearch {
  products: readonly RelatedProduct[] = [
    {id: 7, name: 'Runner', sku: 'ACM-7'},
    {id: 8, name: 'Café Mug', sku: 'ACM-8'},
  ];

  readonly poolSize = 500;

  find(term: string, excludeId: number | null, limit = 10): Observable<readonly RelatedProduct[]> {
    const clean = (value: string) => value.normalize('NFD').replace(MARKS, '').toLowerCase();
    const needle = clean(term).trim();
    return of(
      this.products
        .filter(
          (product) =>
            product.id !== excludeId &&
            (needle === '' || clean(product.name).includes(needle) || clean(product.sku).includes(needle)),
        )
        .slice(0, limit),
    );
  }

  invalidate(): void {
    // Nothing is cached here.
  }
}

/** The provider every spec that mounts a page holding a product picker needs. */
export function provideFakeProductSearch(fake = new FakeProductSearch()) {
  return {provide: ProductSearch, useValue: fake};
}
