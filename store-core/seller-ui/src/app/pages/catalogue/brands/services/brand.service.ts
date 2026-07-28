import {Injectable} from '@angular/core';

import {CrudService} from '../../../shared/services/crud.service';
import {Observable} from 'rxjs';
import {PageT, StorePageRequest} from '../../../common/BaseTable';

@Injectable({
  providedIn: 'root'
})
export class BrandService {

  constructor(
    private readonly crudService: CrudService
  ) {
  }

  getListOfBrands(params: StorePageRequest): Observable<PageT<any>> {
    return this.crudService.get(`/spg/catalog/api/v1/private/manufacturers`, params);
  }

  updateBrand(id: number | string, brand): Observable<any> {
    return this.crudService.put(`/spg/catalog/api/v1/private/manufacturer/${id}`, brand);
  }

  getBrandById(id: number | string): Observable<any> {

    return this.crudService.get(`/spg/catalog/api/v1/private/manufacturer/${id}`);
  }

  createBrand(brand): Observable<any> {
    return this.crudService.post(`/spg/catalog/api/v1/private/manufacturer`, brand);
  }

  deleteBrand(id: number | string): Observable<any> {
    return this.crudService.delete(`/spg/catalog/api/v1/private/manufacturer/${id}`);
  }

  checkBrandCode(code: string): Observable<any> {
    const params = {
      code
    };
    return this.crudService.get(`/spg/catalog/api/v1/private/manufacturer/unique`, params);
  }

}
