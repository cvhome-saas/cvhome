import { Injectable, inject } from '@angular/core';

import {CrudService} from '../../../shared/services/crud.service';
import {Observable} from 'rxjs';
import {PageT, StorePageRequest} from '../../../shared/table/table.types';

export interface TinyProductPageRequest extends StorePageRequest {
  name?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private crudService = inject(CrudService);


  getListOfProducts(params: StorePageRequest): Observable<PageT<any>> {
    //release 3.2.1 use V2
    return this.crudService.get(`/spg/catalog/api/v2/private/base-products`, params);
  }

  getListOfTinyProducts(params: TinyProductPageRequest): Observable<PageT<any>> {
    //release 3.2.1 use V2
    return this.crudService.get(`/spg/catalog/api/v2/private/tiny-products`, params);
  }

  updateProductFromTable(id: string | number, product: any): Observable<any> {
    return this.crudService.patch(`/spg/catalog/api/v1/private/product/${id}`, product);
  }

  updateProduct(id: string | number, product: any): Observable<any> {
    return this.crudService.put(`/spg/catalog/api/v2/private/product/${id}`, product);
  }

  getProductDefinitionById(id: string | number): Observable<any> {
    return this.crudService.get(`/spg/catalog/api/v2/private/product/${id}`);
  }

  createProduct(product: any): Observable<any> {
    return this.crudService.post(`/spg/catalog/api/v2/private/product`, product);
  }

  deleteProduct(id: string | number): Observable<any> {
    return this.crudService.delete(`/spg/catalog/api/v1/private/product/${id}`);
  }

  getProductTypes(): Observable<any> {
    return this.crudService.get(`/spg/catalog/api/v1/private/product/types`);
  }

  checkProductSku(code: string): Observable<any> {
    const params = {
      'code': code,
    };
    return this.crudService.get(`/spg/catalog/api/v1/private/product/unique`, params);
  }

  addProductToCategory(productId: string | number, categoryId: string | number): Observable<any> {
    return this.crudService.post(`/spg/catalog/api/v1/private/product/${productId}/category/${categoryId}}`, {});
  }

  removeProductFromCategory(productId: string | number, categoryId: string | number): Observable<any> {
    return this.crudService.delete(`/spg/catalog/api/v1/private/product/${productId}/category/${categoryId}`);
  }

}
