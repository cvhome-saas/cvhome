import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService, type SpringPage} from '@cvhome-saas/ui-kit';

/**
 * uaa's OAuth2 client registry — the registered clients every other service authenticates as.
 *
 * The first client for `AdminClientController`. Guarded the same way as the user and role APIs:
 * `/api/v1/admin/**` behind `SCOPE_super_admin`/`ROLE_SUPER_ADMIN`.
 *
 * **The list and the detail are different shapes.** `GET /` answers `ClientSummary`
 * — `{id, clientId, clientName}` and nothing more — while `GET /{id}` answers the full
 * `ClientDetails`. A table cannot show grant types or scopes without fetching each row.
 *
 * **Ids here are strings, not UUIDs**, unlike the user and role APIs: the path variable is declared
 * `@PathVariable String id` because a registered client's id is whatever the authorization server
 * assigned it.
 *
 * **A secret is never read back.** Creation returns the client without one and `POST /{id}/reset-secret`
 * answers `void`, so the only moment a caller can show a secret is the one it supplied. That is a
 * property of the endpoint, not an omission here.
 */
export const ADMIN_CLIENT_API_BASE = '/uaa/api/v1/admin/clients';

/** A row in the client list. All the list endpoint returns. */
export interface ClientSummary {
  readonly id: string;
  readonly clientId: string;
  readonly clientName: string;
}

/** Durations arrive as ISO-8601 strings — Java `Duration`, serialized. */
export interface ClientTokenSettings {
  readonly authorizationCodeTimeToLive: string | null;
  readonly accessTokenTimeToLive: string | null;
  readonly accessTokenFormat: string | null;
  readonly deviceCodeTimeToLive: string | null;
  readonly reuseRefreshTokens: boolean;
  readonly refreshTokenTimeToLive: string | null;
  readonly idTokenSignatureAlgorithm: string | null;
  readonly x509CertificateBoundAccessTokens: boolean;
  readonly customSettings: Readonly<Record<string, unknown>>;
}

export interface ClientSettings {
  readonly requireProofKey: boolean;
  readonly requireAuthorizationConsent: boolean;
  readonly jwkSetUrl: string | null;
  readonly tokenEndpointAuthenticationSigningAlgorithm: string | null;
  readonly x509CertificateSubjectDN: string | null;
  readonly customSettings: Readonly<Record<string, unknown>>;
}

export interface ClientDetails {
  readonly id: string;
  readonly clientId: string;
  readonly clientName: string;
  readonly clientAuthenticationMethods: readonly string[];
  readonly authorizationGrantTypes: readonly string[];
  readonly redirectUris: readonly string[];
  readonly postLogoutRedirectUris: readonly string[];
  readonly scopes: readonly string[];
  readonly clientSettings: ClientSettings;
  readonly tokenSettings: ClientTokenSettings;
}

/**
 * What the form may offer, straight from the server's own enums.
 *
 * `GET /options` builds this from `ClientAuthMethod`, `OAuthGrantType` and `SignatureAlgorithm`
 * values, so a form driven by it cannot offer a grant type uaa would reject — which is the reason to
 * fetch it rather than hard-code the lists. `scopes` is the one hand-written entry on the server.
 */
export interface ClientOptions {
  readonly clientAuthenticationMethods: readonly string[];
  readonly authorizationGrantTypes: readonly string[];
  readonly scopes: readonly string[];
  readonly idTokenSignatureAlgorithm: readonly string[];
  readonly tokenEndpointAuthenticationSigningAlgorithm: readonly string[];
  readonly accessTokenFormat: readonly string[];
}

@Injectable({providedIn: 'root'})
export class AdminClientService {
  private readonly crudService = inject(CrudService);

  /** `count`, not Spring's `size` — the platform-wide paging rename applies to uaa too. */
  list(page: number, count: number): Observable<SpringPage<ClientSummary>> {
    return this.crudService.get(ADMIN_CLIENT_API_BASE, {page, count});
  }

  findOne(id: string): Observable<ClientDetails> {
    return this.crudService.get(`${ADMIN_CLIENT_API_BASE}/${id}`);
  }

  create(request: ClientDetails): Observable<ClientDetails> {
    return this.crudService.post(ADMIN_CLIENT_API_BASE, request);
  }

  update(id: string, request: ClientDetails): Observable<ClientDetails> {
    return this.crudService.put(`${ADMIN_CLIENT_API_BASE}/${id}`, request);
  }

  /** Answers `void`: the new secret is only ever known to the caller that set it. */
  resetSecret(id: string, newSecret: string): Observable<void> {
    return this.crudService.post(`${ADMIN_CLIENT_API_BASE}/${id}/reset-secret`, {newSecret});
  }

  delete(id: string): Observable<void> {
    return this.crudService.delete(`${ADMIN_CLIENT_API_BASE}/${id}`);
  }

  /** The enum values a client form may offer. Cheap and static; fetch once per form. */
  options(): Observable<ClientOptions> {
    return this.crudService.get(`${ADMIN_CLIENT_API_BASE}/options`);
  }
}
