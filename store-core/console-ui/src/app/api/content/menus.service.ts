/** Console-native; not a port from seller-core. */
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {Menu, MenuHandle} from '@models/content';
import {CONTENT_PRIVATE} from './content-api';

@Injectable({providedIn: 'root'})
export class MenusService {
  private readonly crudService = inject(CrudService);

  list(): Observable<readonly Menu[]> {
    return this.crudService.get(`${CONTENT_PRIVATE}/menus`);
  }

  get(handle: MenuHandle): Observable<Menu> {
    return this.crudService.get(`${CONTENT_PRIVATE}/menus/${handle}`);
  }

  /** Replaces the whole tree; one level of nesting. */
  put(handle: MenuHandle, body: Menu): Observable<Menu> {
    return this.crudService.put(`${CONTENT_PRIVATE}/menus/${handle}`, body);
  }
}
