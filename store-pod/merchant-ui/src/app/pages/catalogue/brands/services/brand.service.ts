import {Injectable} from '@angular/core';

import {CrudService} from '../../../../shared/services/crud.service';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BrandService {

  constructor(
    private crudService: CrudService
  ) {
  }

  getListOfBrands(params): Observable<any> {
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/manufacturers`, params);
  }

  updateBrand(store, id, brand): Observable<any> {
    return this.crudService.put(`/store-pod-gateway/catalog/api/v1/private/manufacturer/${id}?store=${store}`, brand);
  }

  getBrandById(store, id): Observable<any> {
    const params = {
      store
    };
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/manufacturer/${id}`, params);
  }

  createBrand(store, brand): Observable<any> {
    return this.crudService.post(`/store-pod-gateway/catalog/api/v1/private/manufacturer?store=${store}`, brand);
  }

  deleteBrand(store, id): Observable<any> {
    return this.crudService.delete(`/store-pod-gateway/catalog/api/v1/private/manufacturer/${id}?store=${store}`);
  }

  checkBrandCode(store, code): Observable<any> {
    const params = {
      store,
      code
    };
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/manufacturer/unique`, params);
  }

}
