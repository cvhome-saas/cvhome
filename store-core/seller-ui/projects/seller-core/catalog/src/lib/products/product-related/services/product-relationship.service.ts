import { Injectable, inject } from '@angular/core';
import {CrudService} from 'seller-core';
import {Observable} from 'rxjs';
import {ReadableProductGroup} from '../../../products-groups/models/product-group.model';

@Injectable({
  providedIn: 'root'
})
export class ProductRelationshipService {
  private crudService = inject(CrudService);


  getRelationships(productId: string | number): Observable<ReadableProductGroup> {
    return this.crudService.get(`/spg/catalog/api/v1/products/${productId}/relationship`);
  }

  addProduct(productId: string | number, relatedProductId: string | number): Observable<void> {
    return this.crudService.post(`/spg/catalog/api/v1/private/products/${productId}/relationship/${relatedProductId}`, {});
  }

  removeProduct(productId: string | number, relatedProductId: string | number): Observable<void> {
    return this.crudService.delete(`/spg/catalog/api/v1/private/products/${productId}/relationship/${relatedProductId}`);
  }
}
