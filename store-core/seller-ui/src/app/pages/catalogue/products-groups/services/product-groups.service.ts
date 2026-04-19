import {Injectable} from '@angular/core';

import {CrudService} from '../../../shared/services/crud.service';
import {Observable} from 'rxjs';
import {switchMap} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ProductGroupsService {
  constructor(private crudService: CrudService) {
  }

  getListOfProductGroups(param): Observable<any> {
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/products/groups`, param);
  }

  createProductGroup(group): Observable<any> {
    return this.crudService.post(`/store-pod-gateway/catalog/api/v1/private/products/groups`, group);
  }

  getProductGroup(code, params?): Observable<any> {
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/products/groups/${code}`, params);
  }

  checkCode(code: string): Observable<any> {
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/products/groups/unique`, {code});
  }

  updateGroupActiveValue(group): Observable<any> {
    return this.getProductGroup(group.code).pipe(
      switchMap(current => {
        const updated = {
          code: current.code,
          active: group.active,
          descriptions: (current.descriptions || []).map((d: any) => ({
            language: d.language,
            name: d.name,
          }))
        };
        return this.createProductGroup(updated);
      })
    );
  }

  addProductToGroup(productId, groupCode): Observable<any> {
    return this.crudService.post(`/store-pod-gateway/catalog/api/v1/private/products/groups/${groupCode}/product/${productId}`, {});
  }

  removeProductFromGroup(productId, groupCode) {
    return this.crudService.delete(`/store-pod-gateway/catalog/api/v1/private/products/groups/${groupCode}/product/${productId}`);
  }

  removeProductGroup(groupCode) {
    return this.crudService.delete(`/store-pod-gateway/catalog/api/v1/private/products/groups/${groupCode}`);
  }
}
