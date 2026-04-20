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
    return this.crudService.get(`/spg/catalog/api/v1/private/manufacturers`, params);
  }

  updateBrand(id, brand): Observable<any> {
    return this.crudService.put(`/spg/catalog/api/v1/private/manufacturer/${id}`, brand);
  }

  getBrandById(id): Observable<any> {

    return this.crudService.get(`/spg/catalog/api/v1/private/manufacturer/${id}`);
  }

  createBrand(brand): Observable<any> {
    return this.crudService.post(`/spg/catalog/api/v1/private/manufacturer`, brand);
  }

  deleteBrand(id): Observable<any> {
    return this.crudService.delete(`/spg/catalog/api/v1/private/manufacturer/${id}`);
  }

  checkBrandCode(code:string): Observable<any> {
    const params = {
      code
    };
    return this.crudService.get(`/spg/catalog/api/v1/private/manufacturer/unique`, params);
  }

}
