import {Injectable} from '@angular/core';

import {CrudService} from '../../../shared/services/crud.service';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  constructor(
    private crudService: CrudService,
  ) {
  }

  getListOfProducts(params): Observable<any> {
    //release 3.2.1 use V2
    return this.crudService.get(`/store-pod-gateway/catalog/api/v2/private/base-products`, params);
  }
  getListOfTinyProducts(params): Observable<any> {
    //release 3.2.1 use V2
    return this.crudService.get(`/store-pod-gateway/catalog/api/v2/private/tiny-products`, params);
  }

  updateProductFromTable(id, product): Observable<any> {
    return this.crudService.patch(`/store-pod-gateway/catalog/api/v1/private/product/${id}`, product);
  }

  updateProduct(id, product): Observable<any> {
    return this.crudService.put(`/store-pod-gateway/catalog/api/v2/private/product/${id}`, product);
  }

  getProductDefinitionById( id): Observable<any> {
    return this.crudService.get(`/store-pod-gateway/catalog/api/v2/private/product/${id}`);
  }

  createProduct( product): Observable<any> {
    return this.crudService.post(`/store-pod-gateway/catalog/api/v2/private/product`, product);
  }

  deleteProduct(id): Observable<any> {
    return this.crudService.delete(`/store-pod-gateway/catalog/api/v1/private/product/${id}`);
  }

  getProductTypes(): Observable<any> {
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/product/types`);
  }

  checkProductSku( code): Observable<any> {
    const params = {
      'code': code,
    };
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/product/unique`, params);
  }

  addProductToCategory( productId, categoryId): Observable<any> {
    return this.crudService.post(`/store-pod-gateway/catalog/api/v1/private/product/${productId}/category/${categoryId}}`, {});
  }

  removeProductFromCategory( productId, categoryId): Observable<any> {
    return this.crudService.delete(`/store-pod-gateway/catalog/api/v1/private/product/${productId}/category/${categoryId}`);
  }

}
