/** Console-native; not a port from seller-core. */
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {SiteSettings} from '@models/content';

import {CONTENT_PRIVATE} from './content-api';

/**
 * How the store looks: brand imagery, social links and site-level SEO.
 *
 * One record per store, so there is no id in the path and no create — the first read makes the row. A `PUT`
 * replaces the whole thing, and a `null` media slot clears it: merchant, which used to own the logo and banner,
 * only ever had upload endpoints, so removing one was impossible.
 */
@Injectable({providedIn: 'root'})
export class SiteSettingsService {
  private readonly crudService = inject(CrudService);

  get(): Observable<SiteSettings> {
    return this.crudService.get(`${CONTENT_PRIVATE}/site-settings`);
  }

  put(settings: SiteSettings): Observable<SiteSettings> {
    return this.crudService.put(`${CONTENT_PRIVATE}/site-settings`, settings);
  }
}
