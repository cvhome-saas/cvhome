import {Injectable} from '@angular/core';

import {CrudService} from '../../../../shared/services/crud.service';
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

  updateProductFromTable(store, id, product): Observable<any> {
    return this.crudService.patch(`/store-pod-gateway/catalog/api/v1/private/product/${id}?store=${store}`, product);
  }

  updateProduct(store, id, product): Observable<any> {
    return this.crudService.put(`/store-pod-gateway/catalog/api/v2/private/product/${id}?store=${store}`, product);
  }

  getProductDefinitionById(store, id): Observable<any> {
    const params = {
      store,
    };
    return this.crudService.get(`/store-pod-gateway/catalog/api/v2/private/product/${id}`, params);
  }

  createProduct(store, product): Observable<any> {

    return this.crudService.post(`/store-pod-gateway/catalog/api/v2/private/product?store=${store}`, product);
  }

  deleteProduct(id): Observable<any> {
    return this.crudService.delete(`/store-pod-gateway/catalog/api/v1/private/product/${id}`);
  }

  getProductTypes(store): Observable<any> {
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/product/types?store=${store}`);
  }

  checkProductSku(store, code): Observable<any> {
    const params = {
      'store': store,
      'code': code,
    };
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/product/unique`, params);
  }

  addProductToCategory(store, productId, categoryId): Observable<any> {
    return this.crudService.post(`/store-pod-gateway/catalog/api/v1/private/product/${productId}/category/${categoryId}?store=${store}`, {});
  }

  removeProductFromCategory(store, productId, categoryId): Observable<any> {
    return this.crudService.delete(`/store-pod-gateway/catalog/api/v1/private/product/${productId}/category/${categoryId}?store=${store}`);
  }

}
