import {Injectable} from '@angular/core';

import {CrudService} from '../../../../shared/services/crud.service';
import {Observable} from 'rxjs';

// import { StorageService } from '../../../../shared/services/storage.service';

@Injectable({
  providedIn: 'root'
})
export class ProductGroupsService {
  constructor(
    private crudService: CrudService,
    // private storageService: StorageService
  ) {
  }

  getListOfProductGroups(param): Observable<any> {
    // const store = this.storageService.getMerchant();
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/product/groups`, param);
  }

  // TODO
  // checkGroupCode(code): Observable<any> {
  //   const params = {
  //     'code': code,
  //   };
  //   return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/product/unique`, params);
  // }

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
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/products/group/${groupCode}`, params);
  }

  removeProductGroup(store, groupCode) {
    return this.crudService.delete(`/store-pod-gateway/catalog/api/v1/products/group/${groupCode}?store=${store}`);
  }

}
