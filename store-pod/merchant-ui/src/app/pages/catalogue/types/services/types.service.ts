import {Injectable} from '@angular/core';

import {CrudService} from '../../../../shared/services/crud.service';
import {Observable} from 'rxjs';
import {StorageService} from '../../../../shared/services/storage.service';

@Injectable({
  providedIn: 'root'
})
export class TypesService {

  constructor(
    private crudService: CrudService,
    private storageService: StorageService
  ) {
  }


  getListOfTypes(params): Observable<any> {
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/product/types`, params);
  }

  getType(store, id): Observable<any> {
    const params = {
      store
    };
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/product/type/${id}`, params);
  }

  createType(store, req): Observable<any> {
    return this.crudService.post(`/store-pod-gateway/catalog/api/v1/private/product/type?store=${store}`, req);
  }

  updateType(store, id, req): Observable<any> {
    return this.crudService.put(`/store-pod-gateway/catalog/api/v1/private/product/type/${id}?store=${store}`, req);
  }

  deleteType(store, id): Observable<any> {
    return this.crudService.delete(`/store-pod-gateway/catalog/api/v1/private/product/type/${id}?store=${store}`);
  }

  checkCode(store, code): Observable<any> {
    const params = {
      store,
      code
    };
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/product/type/unique`, params);
  }


}
