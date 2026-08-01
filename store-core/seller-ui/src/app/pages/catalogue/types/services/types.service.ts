import { Injectable, inject } from '@angular/core';

import {CrudService} from '../../../shared/services/crud.service';
import {Observable} from 'rxjs';
import {StorageService} from '../../../shared/services/storage.service';
import {PageT, StorePageRequest} from '../../../shared/table/table.types';

@Injectable({
  providedIn: 'root'
})
export class TypesService {
  private crudService = inject(CrudService);
  private storageService = inject(StorageService);



  getListOfTypes(params: StorePageRequest): Observable<PageT<any>> {
    return this.crudService.get(`/spg/catalog/api/v1/private/product/types`, params);
  }

  getType(id: string | number): Observable<any> {
    return this.crudService.get(`/spg/catalog/api/v1/private/product/type/${id}`);
  }

  createType(req: any): Observable<any> {
    return this.crudService.post(`/spg/catalog/api/v1/private/product/type`, req);
  }

  updateType(id: string | number, req: any): Observable<any> {
    return this.crudService.put(`/spg/catalog/api/v1/private/product/type/${id}`, req);
  }

  deleteType(id: string | number): Observable<any> {
    return this.crudService.delete(`/spg/catalog/api/v1/private/product/type/${id}`);
  }

  checkCode(code: string): Observable<any> {
    const params = {
      code
    };
    return this.crudService.get(`/spg/catalog/api/v1/private/product/type/unique`, params);
  }


}
