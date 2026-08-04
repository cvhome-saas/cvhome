import { Injectable, inject } from '@angular/core';

import {CrudService} from '../../../shared/services/crud.service';
import {Observable} from 'rxjs';
import {PageT, StorePageRequest} from '../../../shared/table/table.types';
import {EntityExists} from '../../../shared/models/entity.model';
import {PersistableCategory, ReadableCategory} from '../models/category.model';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private crudService = inject(CrudService);


  getListOfCategories(params?: Partial<StorePageRequest>): Observable<PageT<ReadableCategory>> {
    return this.crudService.get(`/spg/catalog/api/v1/private/category`, params);
  }


  getCategoryById(id: number | string): Observable<ReadableCategory> {
    return this.crudService.get(`/spg/catalog/api/v1/private/category/${id}`);
  }

  getCategoryByProductId(id: number | string): Observable<PageT<ReadableCategory>> {
    return this.crudService.get(`/spg/catalog/api/v1/private/category/product/${id}`);
  }

  addCategory(category: PersistableCategory): Observable<PersistableCategory> {
    return this.crudService.post(`/spg/catalog/api/v1/private/category`, category);
  }

  updateCategory(id: number | string, category: PersistableCategory): Observable<PersistableCategory> {
    return this.crudService.put(`/spg/catalog/api/v1/private/category/${id}`, category);
  }

  updateCategoryVisibility(category: PersistableCategory): Observable<void> {
    return this.crudService.patch(`/spg/catalog/api/v1/private/category/${category.id}/visible`, category);
  }

  deleteCategory(id: number | string): Observable<void> {
    return this.crudService.delete(`/spg/catalog/api/v1/private/category/${id}`);
  }

  checkCategoryCode(code: string): Observable<EntityExists> {
    const params = {
      'code': code,
    };
    return this.crudService.get(`/spg/catalog/api/v1/private/category/unique`, params);
  }

  getHierarchyOfCategories(params?: Partial<StorePageRequest>): Observable<PageT<ReadableCategory>> {
    return this.crudService.get(`/spg/catalog/api/v1/private/category-hierarchy`, params);
  }

  updateHierarchy(childId: number | string, parentId: number | string): Observable<void> {
    return this.crudService.put(`/spg/catalog/api/v1/private/category/${childId}/move/${parentId}`, {});
  }

}
