/** Console-native; not a port from seller-core. */
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {PostCategory} from '@models/content';
import {CONTENT_PRIVATE} from './content-api';

@Injectable({providedIn: 'root'})
export class PostCategoriesService {
  private readonly crudService = inject(CrudService);

  list(): Observable<readonly PostCategory[]> {
    return this.crudService.get(`${CONTENT_PRIVATE}/posts/categories`);
  }

  create(body: PostCategory): Observable<PostCategory> {
    return this.crudService.post(`${CONTENT_PRIVATE}/posts/categories`, body);
  }

  update(id: number, body: PostCategory): Observable<PostCategory> {
    return this.crudService.put(`${CONTENT_PRIVATE}/posts/categories/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.crudService.delete(`${CONTENT_PRIVATE}/posts/categories/${id}`);
  }
}
