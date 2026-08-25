/** Console-native; not a port from seller-core. */
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {Snippet} from '@models/content';

/**
 * The store's snippets — store-level text fragments the storefront reads by code (`meta-title`,
 * `meta-description`, `header-message`, `agreement`, `LANDING_PAGE`). The content service's
 * `ContentAdminApi` serves them at `/private/content/snippets/{code}`; a `PUT` upserts, so there
 * is no create-versus-update decision to make on the client.
 */
import {CONTENT_PRIVATE} from './content-api';

@Injectable({providedIn: 'root'})
export class SnippetsService {
  private readonly crudService = inject(CrudService);

  /** Every snippet of the store, with every language's copy. */
  list(): Observable<readonly Snippet[]> {
    return this.crudService.get(`${CONTENT_PRIVATE}/snippets`);
  }

  /** One snippet by code, with every language's copy. 404 when the store has never saved it. */
  get(code: string): Observable<Snippet> {
    return this.crudService.get(`${CONTENT_PRIVATE}/snippets/${code}`);
  }

  /** Creates or replaces the snippet of that code. */
  put(code: string, snippet: Snippet): Observable<Snippet> {
    return this.crudService.put(`${CONTENT_PRIVATE}/snippets/${code}`, snippet);
  }
}
