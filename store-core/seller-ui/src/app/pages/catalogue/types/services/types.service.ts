import { Injectable, inject } from '@angular/core';

import {CrudService} from '../../../shared/services/crud.service';
import {Observable} from 'rxjs';
import {PageT, StorePageRequest} from '../../../shared/table/table.types';
import {EntityExists} from '../../../shared/models/entity.model';
import {CreatedEntity, PersistableProductType, ReadableProductType} from '../models/product-type.model';

@Injectable({
  providedIn: 'root'
})
export class TypesService {
  private crudService = inject(CrudService);



  getListOfTypes(params: StorePageRequest): Observable<PageT<ReadableProductType>> {
    return this.crudService.get(`/spg/catalog/api/v1/private/product/types`, params);
  }

  getType(id: string | number): Observable<ReadableProductType> {
    return this.crudService.get(`/spg/catalog/api/v1/private/product/type/${id}`);
  }

  createType(req: PersistableProductType): Observable<CreatedEntity> {
    return this.crudService.post(`/spg/catalog/api/v1/private/product/type`, req);
  }

  updateType(id: string | number, req: PersistableProductType): Observable<void> {
    return this.crudService.put(`/spg/catalog/api/v1/private/product/type/${id}`, req);
  }

  deleteType(id: string | number): Observable<void> {
    return this.crudService.delete(`/spg/catalog/api/v1/private/product/type/${id}`);
  }

  checkCode(code: string): Observable<EntityExists> {
    const params = {
      code
    };
    return this.crudService.get(`/spg/catalog/api/v1/private/product/type/unique`, params);
  }


}
