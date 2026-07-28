import {Injectable, inject} from '@angular/core';

import {CrudService} from '../../../shared/services/crud.service';
import {Observable} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {PageT, StorePageRequest} from '../../../shared/table/table.types';

@Injectable({
  providedIn: 'root'
})
export class ProductGroupsService {
  private readonly crudService = inject(CrudService);

  getListOfProductGroups(params: StorePageRequest): Observable<PageT<any>> {
    return this.crudService.get(`/spg/catalog/api/v1/private/products/groups`, params);
  }

  createProductGroup(group: any): Observable<any> {
    return this.crudService.post(`/spg/catalog/api/v1/private/products/groups`, group);
  }

  getProductGroup(code: string, params?: any): Observable<any> {
    return this.crudService.get(`/spg/catalog/api/v1/private/products/groups/${code}`, params);
  }

  checkCode(code: string): Observable<any> {
    return this.crudService.get(`/spg/catalog/api/v1/private/products/groups/unique`, {code});
  }

  updateGroupActiveValue(group: any): Observable<any> {
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

  addProductToGroup(productId: number | string, groupCode: string): Observable<any> {
    return this.crudService.post(`/spg/catalog/api/v1/private/products/groups/${groupCode}/product/${productId}`, {});
  }

  removeProductFromGroup(productId: number | string, groupCode: string): Observable<any> {
    return this.crudService.delete(`/spg/catalog/api/v1/private/products/groups/${groupCode}/product/${productId}`);
  }

  removeProductGroup(groupCode: string): Observable<any> {
    return this.crudService.delete(`/spg/catalog/api/v1/private/products/groups/${groupCode}`);
  }
}
