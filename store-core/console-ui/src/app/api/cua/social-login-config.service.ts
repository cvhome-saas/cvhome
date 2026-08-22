/** Ported from seller-ui/projects/seller-core/stores/src/lib/services/store.service.ts. */
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {PersistableSocialLoginConfig, ReadableSocialLoginConfig} from '@models/cua';

/**
 * Ported from seller-ui/projects/seller-core/stores/src/lib/services/store.service.ts
 * (`getSupportedSocialLoginProviders`, `getSocialLoginConfigs`, `updateSocialLoginConfigs`),
 * verified against cua's `SocialLoginConfigController`.
 *
 * How a **shopper** signs in to this store's storefront. Nothing to do with `@api/…` staff auth:
 * cua is the customer authorization server, uaa is the staff one, and each has its own
 * `SocialProvider` enum with different members.
 */
const CUA_API_BASE = '/spg/cua/api/v1/private/social-login-config';

@Injectable({providedIn: 'root'})
export class SocialLoginConfigService {
  private readonly crudService = inject(CrudService);

  /**
   * The providers cua can broker a sign-in with — `GOOGLE`, `FACEBOOK`, `GITHUB` today.
   *
   * The enum's own values, serialised by name. Unauthenticated on the server, unlike its siblings.
   */
  supportedProviders(): Observable<string[]> {
    return this.crudService.get(`${CUA_API_BASE}/supported-social-providers`);
  }

  /** Only the providers this store has configured. A provider with no row simply is not in the list. */
  configs(): Observable<ReadableSocialLoginConfig[]> {
    return this.crudService.get(CUA_API_BASE);
  }

  /**
   * Saves configurations, as a list.
   *
   * seller-core typed this as a single object; the controller takes
   * `List<PersistableSocialLoginConfig>` and `saveConfigs` iterates it, so a bare object would bind
   * to nothing. Each entry is an upsert keyed by `(store, provider)` — there is no create-versus-
   * update to decide, and no delete endpoint at all.
   */
  save(configs: readonly PersistableSocialLoginConfig[]): Observable<void> {
    return this.crudService.post(CUA_API_BASE, configs);
  }
}
