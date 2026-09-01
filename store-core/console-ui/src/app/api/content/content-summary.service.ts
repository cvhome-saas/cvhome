/** Console-native; not a port from seller-core. */
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@cvhome-saas/ui-kit';
import type {ContentSummary, Redirect} from '@models/content';
import {CONTENT_PRIVATE} from './content-api';

/** The hub's KPIs and rail counts, and the redirects written by slug changes. */
@Injectable({providedIn: 'root'})
export class ContentSummaryService {
  private readonly crudService = inject(CrudService);

  summary(): Observable<ContentSummary> {
    return this.crudService.get(`${CONTENT_PRIVATE}/summary`);
  }

  redirects(): Observable<readonly Redirect[]> {
    return this.crudService.get(`${CONTENT_PRIVATE}/redirects`);
  }
}
