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

  getType(id): Observable<any> {
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/product/type/${id}`);
  }

  createType(req): Observable<any> {
    return this.crudService.post(`/store-pod-gateway/catalog/api/v1/private/product/type`, req);
  }

  updateType( id, req): Observable<any> {
    return this.crudService.put(`/store-pod-gateway/catalog/api/v1/private/product/type/${id}`, req);
  }

  deleteType( id): Observable<any> {
    return this.crudService.delete(`/store-pod-gateway/catalog/api/v1/private/product/type/${id}`);
  }

  checkCode( code): Observable<any> {
    const params = {
      code
    };
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/product/type/unique`,params);
  }


}
