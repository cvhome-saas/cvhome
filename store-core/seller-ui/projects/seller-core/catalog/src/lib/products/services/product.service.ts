import { Injectable, inject } from '@angular/core';

import {CrudService} from 'seller-core';
import {Observable} from 'rxjs';
import {PageT, StorePageRequest} from 'seller-core';
import {EntityExists} from 'seller-core';
import {
  CreatedProductEntity,
  LightPersistableProduct,
  PersistableProductDefinition,
  ReadableProduct,
  ReadableProductDefinition
} from '../models/product.model';
import {ReadableProductType} from '../../types/models/product-type.model';

export interface TinyProductPageRequest extends StorePageRequest {
  name?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private crudService = inject(CrudService);


  getListOfProducts(params: StorePageRequest): Observable<PageT<ReadableProduct>> {
    //release 3.2.1 use V2
    return this.crudService.get(`/spg/catalog/api/v2/private/base-products`, params);
  }

  getListOfTinyProducts(params: TinyProductPageRequest): Observable<PageT<ReadableProduct>> {
    //release 3.2.1 use V2
    return this.crudService.get(`/spg/catalog/api/v2/private/tiny-products`, params);
  }

  updateProductFromTable(id: string | number, product: LightPersistableProduct): Observable<void> {
    return this.crudService.patch(`/spg/catalog/api/v1/private/product/${id}`, product);
  }

  updateProduct(id: string | number, product: PersistableProductDefinition): Observable<void> {
    return this.crudService.put(`/spg/catalog/api/v2/private/product/${id}`, product);
  }

  getProductDefinitionById(id: string | number): Observable<ReadableProductDefinition> {
    return this.crudService.get(`/spg/catalog/api/v2/private/product/${id}`);
  }

  createProduct(product: PersistableProductDefinition): Observable<CreatedProductEntity> {
    return this.crudService.post(`/spg/catalog/api/v2/private/product`, product);
  }

  deleteProduct(id: string | number): Observable<void> {
    return this.crudService.delete(`/spg/catalog/api/v1/private/product/${id}`);
  }

  getProductTypes(): Observable<PageT<ReadableProductType>> {
    return this.crudService.get(`/spg/catalog/api/v1/private/product/types`);
  }

  checkProductSku(code: string): Observable<EntityExists> {
    const params = {
      'code': code,
    };
    return this.crudService.get(`/spg/catalog/api/v1/private/product/unique`, params);
  }

  addProductToCategory(productId: string | number, categoryId: string | number): Observable<void> {
    return this.crudService.post(`/spg/catalog/api/v1/private/product/${productId}/category/${categoryId}}`, {});
  }

  removeProductFromCategory(productId: string | number, categoryId: string | number): Observable<void> {
    return this.crudService.delete(`/spg/catalog/api/v1/private/product/${productId}/category/${categoryId}`);
  }

}
