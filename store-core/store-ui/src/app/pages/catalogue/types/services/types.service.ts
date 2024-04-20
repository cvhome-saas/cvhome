import {Injectable} from '@angular/core';

import {CrudService} from '../../../shared/services/crud.service';
import {Observable} from 'rxjs';
import {StorageService} from '../../../shared/services/storage.service';

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
    return this.crudService.get(`/store/api/v1/private/product/types`, params);
  }

  getType(store, id, params): Observable<any> {
    return this.crudService.get(`/store/api/v1/private/product/type/${id}?store=${store}`, params);
  }

  createType(store, req): Observable<any> {
    return this.crudService.post(`/store/api/v1/private/product/type?store=${store}`, req);
  }

  updateType(store, id, req): Observable<any> {
    return this.crudService.put(`/store/api/v1/private/product/type/${id}?store=${store}`, req);
  }

  deleteType(store, id): Observable<any> {
    return this.crudService.delete(`/store/api/v1/private/product/type/${id}?store=${store}`);
  }

  checkCode(store, code): Observable<any> {
    return this.crudService.get(`/store/api/v1/private/product/type/unique?code=${code}&store=${store}`);
  }


}
