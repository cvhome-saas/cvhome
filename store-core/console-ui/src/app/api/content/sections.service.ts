/** Console-native; not a port from seller-core. */
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';

import {CONTENT_PRIVATE} from './content-api';

/**
 * The ordering of the home page's blocks.
 *
 * The blocks themselves are ordinary content items, so they go through {@link ContentItemsService} like pages
 * and posts; only the reorder is special. The whole order is sent at once because a reorder that arrives one
 * move at a time leaves gaps and ties, which the storefront would then resolve arbitrarily.
 */
@Injectable({providedIn: 'root'})
export class SectionsService {
  private readonly crudService = inject(CrudService);

  /** Sections the body omits keep their relative order, after the ones it names. */
  reorder(orderedIds: readonly number[]): Observable<void> {
    return this.crudService.patch(`${CONTENT_PRIVATE}/sections/reorder`, orderedIds);
  }
}
