import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {PageRequest, PageT} from '@core/table/table.types';
import type {
  EntityExists,
  PersistableManufacturer,
  ReadableManufacturer,
} from '@models/catalog';

const CATALOG_API_BASE = '/spg/catalog/api/v1';

/**
 * Ported from seller-ui/projects/seller-core/catalog/src/lib/brands/services/brand.service.ts,
 * verified against `catalog-service/api/v1/product/ProductManufacturerApi.java`.
 *
 * Brands. The backend calls them manufacturers and the storefront calls them brands; the console
 * says "brand" to the operator and "manufacturer" on the wire, and this file is the boundary.
 *
 * **seller-core's `ManufactureService` is not ported.** Its one method reads the *public*
 * `/api/v1/manufacturers`, which answers the same list without requiring a session — a second door
 * to data this file already fetches through the private one.
 */
@Injectable({providedIn: 'root'})
export class ManufacturerService {
  private readonly crudService = inject(CrudService);

  list(params?: Partial<PageRequest>): Observable<PageT<ReadableManufacturer>> {
    return this.crudService.get(`${CATALOG_API_BASE}/private/manufacturers`, params);
  }

  get(id: number): Observable<ReadableManufacturer> {
    return this.crudService.get(`${CATALOG_API_BASE}/private/manufacturer/${id}`);
  }

  /** Echoes the body back with the new id. */
  create(brand: PersistableManufacturer): Observable<PersistableManufacturer> {
    return this.crudService.post(`${CATALOG_API_BASE}/private/manufacturer`, brand);
  }

  /** Answers `void` — the page reloads rather than echoing the operator's own input back at them. */
  update(id: number, brand: PersistableManufacturer): Observable<void> {
    return this.crudService.put(`${CATALOG_API_BASE}/private/manufacturer/${id}`, brand);
  }

  delete(id: number): Observable<void> {
    return this.crudService.delete(`${CATALOG_API_BASE}/private/manufacturer/${id}`);
  }

  codeTaken(code: string): Observable<EntityExists> {
    return this.crudService.get(`${CATALOG_API_BASE}/private/manufacturer/unique`, {code});
  }
}
