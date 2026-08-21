import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {PageRequest, PageT} from '@core/table/table.types';
import type {
  CreatedEntity,
  EntityExists,
  PersistableProductType,
  ReadableProductType,
} from '@models/catalog';

const CATALOG_API_BASE = '/spg/catalog/api/v1';

/**
 * Ported from seller-ui/projects/seller-core/catalog/src/lib/types/services/types.service.ts,
 * verified against `catalog-service/api/v1/product/ProductTypeApi.java`.
 *
 * Product types. Note the paths are not symmetric — the list is plural (`/product/types`) and every
 * single-record operation is singular (`/product/type/{id}`). That is the controller's shape, not a
 * transcription slip.
 *
 * **A type is a name and a code, and nothing else.** It carries no attribute definitions, so the
 * design's attribute panel has no backing — see lessons.md, "Catalogue — a product type carries no
 * attribute definitions".
 */
@Injectable({providedIn: 'root'})
export class ProductTypeService {
  private readonly crudService = inject(CrudService);

  list(params?: Partial<PageRequest>): Observable<PageT<ReadableProductType>> {
    return this.crudService.get(`${CATALOG_API_BASE}/private/product/types`, params);
  }

  get(id: number): Observable<ReadableProductType> {
    return this.crudService.get(`${CATALOG_API_BASE}/private/product/type/${id}`);
  }

  /** Echoes just the new id, not the type. */
  create(type: PersistableProductType): Observable<CreatedEntity> {
    return this.crudService.post(`${CATALOG_API_BASE}/private/product/type`, type);
  }

  update(id: number, type: PersistableProductType): Observable<void> {
    return this.crudService.put(`${CATALOG_API_BASE}/private/product/type/${id}`, type);
  }

  delete(id: number): Observable<void> {
    return this.crudService.delete(`${CATALOG_API_BASE}/private/product/type/${id}`);
  }

  codeTaken(code: string): Observable<EntityExists> {
    return this.crudService.get(`${CATALOG_API_BASE}/private/product/type/unique`, {code});
  }
}
