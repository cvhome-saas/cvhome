import {Injectable} from '@angular/core';

import {CrudService} from '../../../shared/services/crud.service';
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
    return this.crudService.get(`/store/api/v1/private/manufacturers`, params);
  }

  updateBrand(store, id, brand): Observable<any> {
    return this.crudService.put(`/store/api/v1/private/manufacturer/${id}?store=${store}`, brand);
  }

  getBrandById(store, id): Observable<any> {
    const params = {
      store,
      lang: '_all'
    };
    return this.crudService.get(`/store/api/v1/manufacturer/${id}`, params);
  }

  createBrand(store, brand): Observable<any> {
    return this.crudService.post(`/store/api/v1/private/manufacturer?store=${store}`, brand);
  }

  deleteBrand(store, id): Observable<any> {
    return this.crudService.delete(`/store/api/v1/private/manufacturer/${id}?store=${store}`);
  }

  checkBrandCode(store, code): Observable<any> {
    const params = {
      store,
      code
    };
    return this.crudService.get(`/store/api/v1/private/manufacturer/unique`, params);
  }

}
