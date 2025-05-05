import {Injectable} from '@angular/core';

import {CrudService} from '../../../../shared/services/crud.service';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProductGroupsService {
  constructor(private crudService: CrudService) {
  }

  getListOfProductGroups(param): Observable<any> {
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/product/groups`, param);
  }


  createProductGroup(store, group): Observable<any> {
    return this.crudService.post(`/store-pod-gateway/catalog/api/v1/private/product/group?store=${store}`, group);
  }

  updateGroupActiveValue(store, group): Observable<any> {
    return this.crudService.patch(`/store-pod-gateway/catalog/api/v1/private/products/group/${group.code}?store=${store}`, group);
  }

  addProductToGroup(store, productId, groupCode): Observable<any> {
    return this.crudService.post(`/store-pod-gateway/catalog/api/v1/private/products/${productId}/group/${groupCode}?store=${store}`, {});
  }

  removeProductFromGroup(store, productId, groupCode) {
    return this.crudService.delete(`/store-pod-gateway/catalog/api/v1/private/products/${productId}/group/${groupCode}?store=${store}`);
  }

  getProductsByGroup(groupCode, params) {
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/products/group/${groupCode}`, params);
  }

  getRelatedProduct(product, params) {
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/products/${product}/related`, params);
  }

  removeProductGroup(store, groupCode) {
    return this.crudService.delete(`/store-pod-gateway/catalog/api/v1/private/products/group/${groupCode}?store=${store}`);
  }

  addProductToRelated(store, product: string, item) {
    return this.crudService.post(`/store-pod-gateway/catalog/api/v1/private/products/${product}/related/${item}?lang=en&store=${store}`, {});
  }

  removeProductFromRelated(store, product: string, item) {
    return this.crudService.delete(`/store-pod-gateway/catalog/api/v1/private/products/${product}/related/${item}?lang=en&store=${store}`, {});

  }
}
