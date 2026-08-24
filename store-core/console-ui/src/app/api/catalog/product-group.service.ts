import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {PageRequest, PageT} from '@core/table/table.types';
import type {
  EntityExists,
  PersistableProductGroup,
  ReadableProductGroup,
} from '@models/catalog';

const CATALOG_API_BASE = '/spg/catalog/api/v1';

/**
 * Verified against `catalog-service/api/v1/product/ProductGroupApi.java`.
 *
 * Product groups: a named, code-addressed membership set. **Not tags** — there is no free-form
 * labelling anywhere on a product; see lessons.md, "Catalogue — no product tags and no collections".
 *
 * **A group is addressed by `code`, never by `id`**, which is unusual on this platform and is why
 * renaming a group's code is not an edit but a new group.
 *
 * **There is no `PUT`.** `POST /private/products/groups` is an upsert keyed on the code, so saving
 * an edited group means re-posting it whole — and a body that omits `descriptions` clears them.
 * seller-core wrapped that in an `updateGroupActiveValue` helper that read the group back first and
 * re-posted it; the console does the same thing in `catalogue.api.service.ts`, where it can reuse
 * the copy the page already has instead of spending a round trip on it.
 */
@Injectable({providedIn: 'root'})
export class ProductGroupService {
  private readonly crudService = inject(CrudService);

  list(params?: Partial<PageRequest>): Observable<PageT<ReadableProductGroup>> {
    return this.crudService.get(`${CATALOG_API_BASE}/private/products/groups`, params);
  }

  get(code: string): Observable<ReadableProductGroup> {
    return this.crudService.get(`${CATALOG_API_BASE}/private/products/groups/${code}`);
  }

  /** Create or replace, keyed on `code`. Echoes the body back. */
  save(group: PersistableProductGroup): Observable<PersistableProductGroup> {
    return this.crudService.post(`${CATALOG_API_BASE}/private/products/groups`, group);
  }

  delete(code: string): Observable<void> {
    return this.crudService.delete(`${CATALOG_API_BASE}/private/products/groups/${code}`);
  }

  addProduct(code: string, productId: number): Observable<void> {
    return this.crudService.post(
      `${CATALOG_API_BASE}/private/products/groups/${code}/product/${productId}`,
      {},
    );
  }

  removeProduct(code: string, productId: number): Observable<void> {
    return this.crudService.delete(
      `${CATALOG_API_BASE}/private/products/groups/${code}/product/${productId}`,
    );
  }

  codeTaken(code: string): Observable<EntityExists> {
    return this.crudService.get(`${CATALOG_API_BASE}/private/products/groups/unique`, {code});
  }
}
