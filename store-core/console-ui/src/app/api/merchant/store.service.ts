/** Ported from seller-ui/projects/seller-core/stores/src/lib/services/store.service.ts. */
import {Injectable, inject} from '@angular/core';
import {Observable, map} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {
  PersistableMerchantStore,
  ReadableMerchantStore,
  SliderImage,
} from '@models/merchant';

/**
 * Ported from seller-ui/projects/seller-core/stores/src/lib/services/store.service.ts, verified
 * method by method against `merchant-service/api/v1/MerchantStoreApi.java`.
 *
 * The store as the merchant pod owns it — its identity, its marketing images, its social links.
 * Distinct from `@api/tenancy/manager-store.service.ts`, which is tenancy's row about the store
 * (pod placement, provisioning, the store list) and cannot edit any of this.
 *
 * Every call here is scoped by the request context's `store` and `pod` params, because the
 * settings page always edits the store the console is currently working in. The controller reads
 * that as its `StoreMerchantId` argument.
 *
 * **Two of seller-core's methods are deliberately not ported.** `removeStoreLogo` and
 * `removeStoreBanner` post to `/v1/private/store/{store}/marketing/logo|banner` — paths missing
 * the `/spg/merchant/api` prefix every sibling carries, and mapped by no controller.
 * `MerchantStoreApi` has `addLogo` and `addBanner` and no delete counterpart, and
 * `PersistableMerchantStore` carries neither image, so an update cannot clear one either. Removing
 * a logo is simply not expressible. See lessons.md, "Store management — a logo or banner can be
 * uploaded but never removed". `updateSocialNetworks` is not ported either: no caller exists
 * anywhere in seller-ui and `POST /v1/private/store/{store}/marketing` is likewise unmapped.
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

  /**
   * Replaces the store's social links.
   *
   * Takes a whole `PersistableMerchantStore` even though the controller reads only
   * `getSocialLinks()` off it — that is the declared body type, not a convenience.
   */
  updateSocialLinks(store: PersistableMerchantStore): Observable<void> {
    return this.crudService.put(`${MERCHANT_STORE_API_BASE}/private/store/social-links`, store);
  }

  /**
   * Replaces the slider in one call, which is what makes reordering and deleting possible.
   *
   * There is no delete-slide and no reorder endpoint. Both are expressed by sending the list you
   * want: drop an entry to delete it, renumber `priority` to reorder. Same store-shaped body as
   * `updateSocialLinks`, for the same reason.
   */
  updateSliderImages(store: PersistableMerchantStore): Observable<void> {
    return this.crudService.put(`${MERCHANT_STORE_API_BASE}/private/store/marketing/slider-images`, store);
  }

  addLogo(file: File): Observable<void> {
    return this.crudService.post(`${MERCHANT_STORE_API_BASE}/private/store/marketing/logo`, this.upload(file));
  }

  addBanner(file: File): Observable<void> {
    return this.crudService.post(`${MERCHANT_STORE_API_BASE}/private/store/marketing/banner`, this.upload(file));
  }

  /** Answers with the stored slide, whose `name` is a server-issued UUID rather than the filename. */
  addSliderImage(file: File): Observable<SliderImage> {
    return this.crudService.post(
      `${MERCHANT_STORE_API_BASE}/private/store/marketing/add-slider-image`,
      this.upload(file),
    );
  }

  /**
   * The controller takes `@RequestParam("file") MultipartFile`, so the part must be named `file`.
   * `HttpClient` sets the multipart boundary itself; setting a `Content-Type` here would break it.
   */
  private upload(file: File): FormData {
    const body = new FormData();
    body.append('file', file, file.name);
    return body;
  }
}
