import {Injectable, inject} from '@angular/core';

import {CrudService} from '../../../shared/services/crud.service';
import {Observable} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {PageT, StorePageRequest} from '../../../shared/table/table.types';
import {EntityExists} from '../../../shared/models/entity.model';
import {PersistableProductGroup, ReadableProductGroup} from '../models/product-group.model';

@Injectable({
  providedIn: 'root'
})
export class ProductGroupsService {
  private readonly crudService = inject(CrudService);

  getListOfProductGroups(params: StorePageRequest): Observable<PageT<ReadableProductGroup>> {
    return this.crudService.get(`/spg/catalog/api/v1/private/products/groups`, params);
  }

  createProductGroup(group: PersistableProductGroup): Observable<PersistableProductGroup> {
    return this.crudService.post(`/spg/catalog/api/v1/private/products/groups`, group);
  }

  getProductGroup(code: string, params?: Record<string, string>): Observable<ReadableProductGroup> {
    return this.crudService.get(`/spg/catalog/api/v1/private/products/groups/${code}`, params);
  }

  checkCode(code: string): Observable<EntityExists> {
    return this.crudService.get(`/spg/catalog/api/v1/private/products/groups/unique`, {code});
  }

  updateGroupActiveValue(group: ReadableProductGroup): Observable<PersistableProductGroup> {
    return this.getProductGroup(group.code).pipe(
      switchMap(current => {
        const updated: PersistableProductGroup = {
          code: current.code,
          active: group.active,
          descriptions: (current.descriptions || []).map((d) => ({
            language: d.language,
            name: d.name,
          }))
        };
        return this.createProductGroup(updated);
      })
    );
  }

  addProductToGroup(productId: number | string, groupCode: string): Observable<void> {
    return this.crudService.post(`/spg/catalog/api/v1/private/products/groups/${groupCode}/product/${productId}`, {});
  }

  removeProductFromGroup(productId: number | string, groupCode: string): Observable<void> {
    return this.crudService.delete(`/spg/catalog/api/v1/private/products/groups/${groupCode}/product/${productId}`);
  }

  removeProductGroup(groupCode: string): Observable<void> {
    return this.crudService.delete(`/spg/catalog/api/v1/private/products/groups/${groupCode}`);
  }
}
