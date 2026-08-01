import { Injectable, inject } from '@angular/core';

import {CrudService} from '../../../shared/services/crud.service';
import {Observable} from 'rxjs';
import {ReadableImage} from '../models/product.model';

@Injectable({
  providedIn: 'root'
})
export class ProductImageService {
  private crudService = inject(CrudService);


  addImageUrl(id: string | number): string {//post
    return this.crudService.getBaseUrl() + `/spg/catalog/api/v1/private/product/${id}/image`;
  }

  removeImageUrl(id: string | number): string {//delete
    return this.crudService.getBaseUrl() + `/spg/catalog/api/v1/private/product/${id}/image`;
  }

  getImages(productId: string | number): Observable<ReadableImage[]> {
    return this.crudService.get(`/spg/catalog/api/v1/product/${productId}/images`);
  }

  removeImage(productId: string | number, imageId: string | number): Observable<void> {
    return this.crudService.delete(`/spg/catalog/api/v1/private/product/${productId}/image/${imageId}`);
  }

  createImage(id: string | number, uploadData: FormData): Observable<void> {
    return this.crudService.post(`/spg/catalog/api/v1/private/product/${id}/images`, uploadData);
  }

  updateImage(productId: string | number, event: { id: string | number; position: number }): Observable<void> {
    return this.crudService.patch(`/spg/catalog/api/v1/private/product/${productId}/image/${event.id}?order=${event.position}`, []);
  }

}
