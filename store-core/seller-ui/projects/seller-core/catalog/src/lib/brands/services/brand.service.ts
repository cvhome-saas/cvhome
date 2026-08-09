import { Injectable, inject } from '@angular/core';

import {CrudService} from 'seller-core';
import {Observable} from 'rxjs';
import {PageT, StorePageRequest} from 'seller-core';
import {EntityExists} from 'seller-core';
import {PersistableManufacturer, ReadableManufacturer} from '../models/brand.model';

@Injectable({
  providedIn: 'root'
})
export class BrandService {
  private readonly crudService = inject(CrudService);


  getListOfBrands(params: StorePageRequest): Observable<PageT<ReadableManufacturer>> {
    return this.crudService.get(`/spg/catalog/api/v1/private/manufacturers`, params);
  }

  updateBrand(id: number | string, brand: PersistableManufacturer): Observable<void> {
    return this.crudService.put(`/spg/catalog/api/v1/private/manufacturer/${id}`, brand);
  }

  getBrandById(id: number | string): Observable<ReadableManufacturer> {

    return this.crudService.get(`/spg/catalog/api/v1/private/manufacturer/${id}`);
  }

  createBrand(brand: PersistableManufacturer): Observable<PersistableManufacturer> {
    return this.crudService.post(`/spg/catalog/api/v1/private/manufacturer`, brand);
  }

  deleteBrand(id: number | string): Observable<void> {
    return this.crudService.delete(`/spg/catalog/api/v1/private/manufacturer/${id}`);
  }

  checkBrandCode(code: string): Observable<EntityExists> {
    const params = {
      code
    };
    return this.crudService.get(`/spg/catalog/api/v1/private/manufacturer/unique`, params);
  }

}
