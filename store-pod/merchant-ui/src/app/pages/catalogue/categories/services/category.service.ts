import {Injectable} from '@angular/core';

import {CrudService} from '../../../../shared/services/crud.service';
import {StorageService} from '../../../../shared/services/storage.service';
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
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/category`, params);
  }


  getCategoryById(id, store): Observable<any> {
    const params = {
      lang: '_all'
    };
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/category/${id}?store=${store}`, params);
  }

  getCategoryByProductId(id, store, lang): Observable<any> {
    const params = {
      store: store,
      lang
    };
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/category/product/${id}`, params);
  }

  addCategory(category, store): Observable<any> {
    return this.crudService.post(`/store-pod-gateway/catalog/api/v1/private/category?store=${store}`, category);
  }

  updateCategory(id, category, store): Observable<any> {
    return this.crudService.put(`/store-pod-gateway/catalog/api/v1/private/category/${id}?store=${store}`, category);
  }

  updateCategoryVisibility(category, store): Observable<any> {
    return this.crudService.patch(`/store-pod-gateway/catalog/api/v1/private/category/${category.id}/visible?store=${store}`, category);
  }

  deleteCategory(id, store): Observable<any> {
    return this.crudService.delete(`/store-pod-gateway/catalog/api/v1/private/category/${id}?store=${store}`);
  }

  checkCategoryCode(code, store): Observable<any> {
    const params = {
      'code': code,
    };
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/category/unique?store=${store}`, params);
  }

  getHierarchyOfCategories(params?): Observable<any> {
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/private/category-hierarchy`, params);
  }

  updateHierarchy(childId, parentId, store): Observable<any> {
    return this.crudService.put(`/store-pod-gateway/catalog/api/v1/private/category/${childId}/move/${parentId}?store=${store}`, {});
  }

}
