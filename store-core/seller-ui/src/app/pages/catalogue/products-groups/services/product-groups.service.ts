import {Injectable} from '@angular/core';

import {CrudService} from '../../../shared/services/crud.service';
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


  createProductGroup( group): Observable<any> {
    return this.crudService.post(`/store-pod-gateway/catalog/api/v1/private/products/group`, group);
  }

  updateGroupActiveValue( group): Observable<any> {
    return this.crudService.patch(`/store-pod-gateway/catalog/api/v1/private/products/group/${group.code}`, group);
  }

  addProductToGroup(productId, groupCode): Observable<any> {
    return this.crudService.post(`/store-pod-gateway/catalog/api/v1/private/products/${productId}/group/${groupCode}`, {});
  }

  removeProductFromGroup( productId, groupCode) {
    return this.crudService.delete(`/store-pod-gateway/catalog/api/v1/private/products/${productId}/group/${groupCode}`);
  }

  getProductsByGroup(groupCode, params) {
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/products/group/${groupCode}`, params);
  }

  getRelatedProduct(product, params) {
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/products/${product}/related`, params);
  }

  removeProductGroup( groupCode) {
    return this.crudService.delete(`/store-pod-gateway/catalog/api/v1/private/products/group/${groupCode}`);
  }

  addProductToRelated( product: string, item) {
    return this.crudService.post(`/store-pod-gateway/catalog/api/v1/private/products/${product}/related/${item}`, {});
  }

  removeProductFromRelated(product: string, item) {
    return this.crudService.delete(`/store-pod-gateway/catalog/api/v1/private/products/${product}/related/${item}`, {});

  }
}
