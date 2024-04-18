import {Injectable} from '@angular/core';

import {CrudService} from '../../../shared/services/crud.service';
import {Observable} from 'rxjs';
import {PRIMARY_OUTLET, Router, UrlSegment, UrlSegmentGroup, UrlTree} from '@angular/router';
import {Location} from '@angular/common';

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
    return this.crudService.get(`/store/api/v2/products`, params);
  }

  updateProductFromTable(store, id, product): Observable<any> {
    return this.crudService.patch(`/store/api/v1/private/product/${id}?store=${store}`, product);
  }

  updateProduct(store, id, product): Observable<any> {
    return this.crudService.put(`/store/api/v2/private/product/${id}?store=${store}`, product);
  }

  getProductById(id): Observable<any> {
    const params = {
      lang: '_all'
    };
    return this.crudService.get(`/store/api/v1/product/${id}`, params);
  }

  getProductDefinitionById(id): Observable<any> {
    const params = {
      lang: '_all'
    };
    return this.crudService.get(`/store/api/v2/private/product/definition/${id}`, params);
  }

  getProductDefinitionBySku(sku): Observable<any> {
    const params = {
      lang: '_all'
    };
    return this.crudService.get(`/store/api/v2/product/${sku}`, params);
  }

  createProduct(store, product): Observable<any> {

    return this.crudService.post(`/store/api/v2/private/product/definition?store=${store}`, product);
  }

  deleteProduct(id): Observable<any> {
    return this.crudService.delete(`/store/api/v1/private/product/${id}`);
  }

  getProductTypes(store): Observable<any> {
    return this.crudService.get(`/store/api/v1/private/product/types?store=${store}`);
  }

  checkProductSku(store, code): Observable<any> {
    const params = {
      'store': store,
      'code': code,
    };
    return this.crudService.get(`/store/api/v1/private/product/unique`, params);
  }

  addProductToCategory(productId, categoryId): Observable<any> {
    return this.crudService.post(`/store/api/v1/private/product/${productId}/category/${categoryId}`, {});
  }

  removeProductFromCategory(productId, categoryId): Observable<any> {
    return this.crudService.delete(`/store/api/v1/private/product/${productId}/category/${categoryId}`);
  }

  getProductByOrder(): Observable<any> {
    return this.crudService.get(`/store/api/v1/product?count=200&lang=en&page=0`)
  }

  getProductOrderById(id): Observable<any> {
    return this.crudService.get(`/store/api/v1/product?category=${id}&count=200&lang=en&page=0`)
  }

  getProductIdRoute(router: Router, location: Location) {
    const tree: UrlTree = router.parseUrl(location.path());
    const g: UrlSegmentGroup = tree.root.children[PRIMARY_OUTLET];
    const s: UrlSegment[] = g.segments; // returns 2 segments
    return s[4].path;
  }

}
