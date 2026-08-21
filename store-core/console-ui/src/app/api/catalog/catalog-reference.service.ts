import {Injectable, inject} from '@angular/core';
import {Observable, catchError, shareReplay, throwError} from 'rxjs';

import {CategoryService} from '@api/catalog/category.service';
import {ManufacturerService} from '@api/catalog/manufacturer.service';
import {ProductTypeService} from '@api/catalog/product-type.service';
import {MerchantStoreService} from '@api/merchant/store.service';
import {SelectedStoreService} from '@api/tenancy/selected-store.service';
import type {PageT} from '@core/table/table.types';
import type {ReadableCategory, ReadableManufacturer, ReadableProductType} from '@models/catalog';
import type {ReadableMerchantStore} from '@models/merchant';

/** One page, large enough that no store's taxonomy pages. The catalogue uses the same figure. */
const REFERENCE_PAGE = {page: 0, count: 500} as const;

/**
 * The four lists every catalogue-adjacent screen needs, read once per store.
 *
 * The category hierarchy, the brands, the product types and the store's languages are **reference
 * data**: they change when someone edits the catalogue, which is rare, and they are needed by the
 * product form, the products list and the catalogue itself, which is constant. Without this, moving
 * between the list and a product re-read all four every time.
 *
 * **The catalogue does not read through this**, deliberately. It is the screen that *edits* this
 * data, so it must always see the server's answer rather than its own cache; it calls `invalidate`
 * after every write instead, which is what keeps the product form's copy honest.
 */
@Injectable({providedIn: 'root'})
export class CatalogReference {
  private readonly categories = inject(CategoryService);
  private readonly brands = inject(ManufacturerService);
  private readonly types = inject(ProductTypeService);
  private readonly stores = inject(MerchantStoreService);
  /*
   * The api tier's own source of the current store — the same one `SelectedStoreRequestContext` uses
   * to stamp `?store=` on the outgoing request. Reading it from the console shell would point this
   * tier at the UI, which the lint rule forbids and which would be the wrong direction anyway.
   */
  private readonly selected = inject(SelectedStoreService);

  private readonly cache = new Map<string, Observable<unknown>>();

  hierarchy(): Observable<PageT<ReadableCategory>> {
    return this.once('hierarchy', () => this.categories.hierarchy(REFERENCE_PAGE));
  }

  brandList(): Observable<PageT<ReadableManufacturer>> {
    return this.once('brands', () => this.brands.list(REFERENCE_PAGE));
  }

  typeList(): Observable<PageT<ReadableProductType>> {
    return this.once('types', () => this.types.list(REFERENCE_PAGE));
  }

  store(): Observable<ReadableMerchantStore> {
    return this.once('store', () => this.stores.store());
  }

  /** Drops everything, so the next read goes to the server. Called after any taxonomy write. */
  invalidate(): void {
    this.cache.clear();
  }

  /**
   * One shared read per store.
   *
   * Keyed on the store id as well as the list, because these are per-store and a switch must not
   * serve one store's brands under another's name — the same reason the products resource keys on
   * it. `refCount: false` so the value survives the last subscriber going away, which is exactly
   * what happens when the operator navigates from the list to a product.
   */
  private once<T>(name: string, read: () => Observable<T>): Observable<T> {
    const key = `${this.selected.currentSelectedStore()?.id ?? ''}:${name}`;
    const existing = this.cache.get(key) as Observable<T> | undefined;
    if (existing) {
      return existing;
    }
    const shared = read().pipe(
      /*
       * A failure is not cached. `shareReplay` remembers an error as faithfully as a value, so
       * without this a single 404 — a service still warming up, a network blip — would be replayed
       * to every reader for the rest of the session, and the currency or the brand list would stay
       * missing until a catalogue write happened to clear the cache. Dropping the entry means the
       * next reader tries again.
       */
      catchError((failure: unknown) => {
        this.cache.delete(key);
        return throwError(() => failure);
      }),
      shareReplay({bufferSize: 1, refCount: false}),
    );
    this.cache.set(key, shared);
    return shared;
  }
}
