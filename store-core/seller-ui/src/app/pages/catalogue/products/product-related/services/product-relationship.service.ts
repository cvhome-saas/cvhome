import { Injectable, inject } from '@angular/core';
import {CrudService} from '../../../../shared/services/crud.service';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProductRelationshipService {
  private crudService = inject(CrudService);


  getRelationships(productId: string | number): Observable<any> {
    return this.crudService.get(`/spg/catalog/api/v1/products/${productId}/relationship`);
  }

  addProduct(productId: string | number, relatedProductId: string | number): Observable<any> {
    return this.crudService.post(`/spg/catalog/api/v1/private/products/${productId}/relationship/${relatedProductId}`, {});
  }

  removeProduct(productId: string | number, relatedProductId: string | number): Observable<any> {
    return this.crudService.delete(`/spg/catalog/api/v1/private/products/${productId}/relationship/${relatedProductId}`);
  }
}
