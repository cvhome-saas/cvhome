import {Injectable} from '@angular/core';

import {CrudService} from '../../../shared/services/crud.service';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {

  constructor(
    private crudService: CrudService
  ) {
  }

  getListOfCategories(params?): Observable<any> {
    return this.crudService.get(`/spg/catalog/api/v1/private/category`, params);
  }


  getCategoryById(id): Observable<any> {
    return this.crudService.get(`/spg/catalog/api/v1/private/category/${id}`);
  }

  getCategoryByProductId(id): Observable<any> {
    return this.crudService.get(`/spg/catalog/api/v1/private/category/product/${id}`);
  }

  addCategory(category): Observable<any> {
    return this.crudService.post(`/spg/catalog/api/v1/private/category`, category);
  }

  updateCategory(id, category): Observable<any> {
    return this.crudService.put(`/spg/catalog/api/v1/private/category/${id}`, category);
  }

  updateCategoryVisibility(category): Observable<any> {
    return this.crudService.patch(`/spg/catalog/api/v1/private/category/${category.id}/visible`, category);
  }

  deleteCategory(id): Observable<any> {
    return this.crudService.delete(`/spg/catalog/api/v1/private/category/${id}`);
  }

  checkCategoryCode(code): Observable<any> {
    const params = {
      'code': code,
    };
    return this.crudService.get(`/spg/catalog/api/v1/private/category/unique`, params);
  }

  getHierarchyOfCategories(params?): Observable<any> {
    return this.crudService.get(`/spg/catalog/api/v1/private/category-hierarchy`, params);
  }

  updateHierarchy(childId, parentId): Observable<any> {
    return this.crudService.put(`/spg/catalog/api/v1/private/category/${childId}/move/${parentId}`, {});
  }

}
