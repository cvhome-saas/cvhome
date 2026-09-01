import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@cvhome-saas/ui-kit';
import type {PageRequest, PageT} from '@cvhome-saas/ui-kit';
import type {
  CreatedEntity,
  EntityExists,
  PersistableProductOption,
  ReadableProductOption,
} from '@models/catalog';

const CATALOG_API_BASE = '/spg/catalog/api/v1';

/**
 * The store's option vocabulary — Color, Size and whatever else its products vary by. Defined once
 * per store; products assign options per product, and the storefront never reads this API directly.
 *
 * Paths follow the product type's asymmetry: the list is plural (`/product/options`), every
 * single-record operation singular (`/product/option/{id}`). Writes are whole documents — the
 * option and all its values travel together, and a value carrying its id keeps its row, which is
 * what keeps store-wide value ids stable while an operator renames "Blue" to "Navy".
 *
 * `DELETE` answers **409** while any product assigns the option or a variant uses one of its
 * values — the pod refuses rather than orphaning variants. The facade names that refusal.
 */
@Injectable({providedIn: 'root'})
export class ProductOptionService {
  private readonly crudService = inject(CrudService);

  list(params?: Partial<PageRequest>): Observable<PageT<ReadableProductOption>> {
    return this.crudService.get(`${CATALOG_API_BASE}/private/product/options`, params);
  }

  get(id: number): Observable<ReadableProductOption> {
    return this.crudService.get(`${CATALOG_API_BASE}/private/product/option/${id}`);
  }

  /** Echoes just the new id, not the option. */
  create(option: PersistableProductOption): Observable<CreatedEntity> {
    return this.crudService.post(`${CATALOG_API_BASE}/private/product/option`, option);
  }

  update(id: number, option: PersistableProductOption): Observable<void> {
    return this.crudService.put(`${CATALOG_API_BASE}/private/product/option/${id}`, option);
  }

  /** 409 while the option is assigned to a product or one of its values is used by a variant. */
  delete(id: number): Observable<void> {
    return this.crudService.delete(`${CATALOG_API_BASE}/private/product/option/${id}`);
  }

  codeTaken(code: string): Observable<EntityExists> {
    return this.crudService.get(`${CATALOG_API_BASE}/private/product/option/unique`, {code});
  }
}
