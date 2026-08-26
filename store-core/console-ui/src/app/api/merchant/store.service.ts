import {Injectable, inject} from '@angular/core';
import {Observable, map} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {
  PersistableMerchantStore,
  ReadableMerchantStore,
} from '@models/merchant';

/**
 * The store as the merchant pod owns it — its identity, its languages, its currency, its domains.
 * Distinct from `@api/tenancy/manager-store.service.ts`, which is tenancy's row about the store
 * (pod placement, provisioning, the store list) and cannot edit any of this.
 *
 * Every call here is scoped by the request context's `store` and `pod` params, because the
 * settings page always edits the store the console is currently working in. The controller reads
 * that as its `StoreMerchantId` argument.
 *
 * **Appearance is not here.** The logo, banner, slider images and social links moved to the content
 * service, which owns the media library they come from — see `@api/content/site-settings.service.ts`.
 * That also closed a hole this comment used to describe: merchant had upload endpoints and no delete
 * counterpart, so a logo could be set and never removed. Clearing a slot is an ordinary `PUT` now.
 */
const MERCHANT_STORE_API_BASE = '/spg/merchant/api/v1';

@Injectable({providedIn: 'root'})
export class MerchantStoreService {
  private readonly crudService = inject(CrudService);

  /**
   * The whole store, as the settings page needs it — identity, address, logo, banner, slider
   * images, social links and domains in one answer.
   *
   * `GET /private/store` rather than tenancy's `store-manager/private/store/{code}`: both end at
   * the same pod, but this one is scoped by the request context instead of a path variable, and it
   * is the merchant pod answering about itself rather than tenancy proxying and merging.
   */
  store(): Observable<ReadableMerchantStore> {
    return this.crudService.get(`${MERCHANT_STORE_API_BASE}/private/store`);
  }

  /**
   * The language codes this store trades in.
   *
   * Answers with `List<LanguageCode>` and `LanguageCode` is a record, so the wire shape is
   * `[{"code":"ar"}, {"code":"en"}]` — objects, not the bare strings the same codes arrive as on
   * `ReadableMerchantStore.supportedLanguages`, where a serializer flattens them. Unwrapped here so
   * one shape reaches the console: bound straight through, a `<select>` renders `[object Object]`.
   */
  supportedLanguages(): Observable<string[]> {
    return this.crudService
      .get<{code: string}[]>(`${MERCHANT_STORE_API_BASE}/store/languages`)
      .pipe(map((languages) => languages.map((language) => language?.code).filter(Boolean)));
  }

  update(store: PersistableMerchantStore): Observable<void> {
    return this.crudService.put(`${MERCHANT_STORE_API_BASE}/private/store`, store);
  }

  /**
   * Deletes the store outright.
   *
   * The server refuses to remove an org's default store (`DefaultStoreNotRemovableException`), so
   * a 4xx here is a rule rather than a fault and the page says which.
   */
  delete(): Observable<void> {
    return this.crudService.delete(`${MERCHANT_STORE_API_BASE}/private/store`);
  }

}
