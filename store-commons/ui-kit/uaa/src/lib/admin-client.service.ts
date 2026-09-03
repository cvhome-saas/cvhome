import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService, UI_KIT_CONFIG, type SpringPage} from '@cvhome-saas/ui-kit';

/**
 * uaa's OAuth2 client registry — the registered clients every other service authenticates as.
 *
 * The first client for `AdminClientController`. Guarded the same way as the user and role APIs:
 * `/api/v1/admin/**` behind `SCOPE_super_admin`/`ROLE_SUPER_ADMIN`.
 *
 * **The list and the detail are different shapes.** `GET /` answers `ClientSummary` — enough to draw
 * the table: type, enabled, grant types, the secret's expiry, the last token — while `GET /{id}`
 * answers the full `ClientDetails` with its `status`.
 *
 * **Ids here are strings, not UUIDs**, unlike the user and role APIs: the path variable is declared
 * `@PathVariable String id` because a registered client's id is whatever the authorization server
 * assigned it.
 *
 * **A secret appears in exactly two responses** — {@link create} and {@link rotateSecret} — and can
 * never be read again: only its hash is stored. Show it in `app-one-time-link-dialog`, never in a toast.
 */
export const ADMIN_CLIENT_API_PATH = '/api/v1/admin/clients';

/**
 * Derived on the server from how a client authenticates and what it may ask for; never stored.
 * `PUBLIC` authenticates with `none` (PKCE only), `MACHINE` holds a secret and asks only for
 * `client_credentials`, `CONFIDENTIAL` holds a secret and signs users in.
 */
export type ClientType = 'PUBLIC' | 'MACHINE' | 'CONFIDENTIAL';

/** A row in the client list. */
export interface ClientSummary {
  readonly id: string;
  readonly clientId: string;
  readonly clientName: string;
  readonly type: ClientType;
  readonly enabled: boolean;
  readonly grantTypes: readonly string[];
  readonly clientSecretExpiresAt: string | null;
  readonly lastTokenIssuedAt: string | null;
}

/** The list's filters; every part is optional and they combine with AND. */
export interface ClientSearch {
  readonly q?: string;
  readonly enabled?: boolean | null;
  readonly type?: ClientType | '';
}

/** The tiles above the list. */
export interface ClientStats {
  readonly total: number;
  readonly enabled: number;
  readonly disabled: number;
  readonly machine: number;
  readonly confidential: number;
  readonly publicClients: number;
  readonly secretsExpiringSoon: number;
}

/**
 * What uaa adds to a registration beyond Spring's settings. Only `description` is writable through
 * `create`/`update`; enabling and the secret's lifetime have their own endpoints.
 */
export interface ClientStatus {
  readonly description: string | null;
  readonly enabled: boolean;
  readonly type: ClientType;
  readonly clientIdIssuedAt: string | null;
  readonly clientSecretExpiresAt: string | null;
  readonly lastTokenIssuedAt: string | null;
  readonly disabledAt: string | null;
  readonly disabledBy: string | null;
  /** When the rotated-out secret stops authenticating, or null when none is in its grace window. */
  readonly previousSecretUntil: string | null;
}

/** `POST /` answers the registration and, once, its generated secret — `null` for a public client. */
export interface CreatedClient {
  readonly client: ClientDetails;
  readonly clientSecret: string | null;
}

/** `POST /{id}/rotate-secret` answers the new secret once, with both lifetimes. */
export interface RotatedSecret {
  readonly id: string;
  readonly clientId: string;
  readonly clientSecret: string;
  readonly clientSecretExpiresAt: string | null;
  readonly previousSecretUntil: string | null;
}

/** Durations arrive as ISO-8601 strings — Java `Duration`, serialized. */
export interface ClientTokenSettings {
  readonly authorizationCodeTimeToLive: string | null;
  readonly accessTokenTimeToLive: string | null;
  /**
   * `{value: 'self-contained'}` — an `OAuth2TokenFormat`, which serializes as an object with one
   * field, not as the bare string the name suggests. Declared as it really is: it has no screen and
   * is only ever carried back unchanged, and a `string` here would make the first caller that reads
   * it print `[object Object]`.
   */
  readonly accessTokenFormat: {readonly value: string} | null;
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
  /** Null on a request that has nothing to say about it; always present on a read. */
  readonly status: ClientStatus | null;
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
  readonly clientTypes: readonly ClientType[];
}

@Injectable({providedIn: 'root'})
export class AdminClientService {
  private readonly crudService = inject(CrudService);
  /** `/uaa/…` behind the gateway, `/api/…` on uaa itself. See {@link UiKitConfig.uaaBasePath}. */
  private readonly base = `${inject(UI_KIT_CONFIG).uaaBasePath}${ADMIN_CLIENT_API_PATH}`;

  /** `count`, not Spring's `size` — the platform-wide paging rename applies to uaa too. */
  list(page: number, count: number, search: ClientSearch = {}): Observable<SpringPage<ClientSummary>> {
    const params: Record<string, string | number | boolean> = {page, count};
    if (search.q?.trim()) {
      params['q'] = search.q.trim();
    }
    if (search.enabled === true || search.enabled === false) {
      params['enabled'] = search.enabled;
    }
    if (search.type) {
      params['type'] = search.type;
    }
    return this.crudService.get(this.base, params);
  }

  stats(): Observable<ClientStats> {
    return this.crudService.get(`${this.base}/stats`);
  }

  findOne(id: string): Observable<ClientDetails> {
    return this.crudService.get(`${this.base}/${id}`);
  }

  /** 201 with the generated secret, once. */
  create(request: ClientDetails): Observable<CreatedClient> {
    return this.crudService.post(this.base, request);
  }

  enable(id: string): Observable<ClientDetails> {
    return this.crudService.post(`${this.base}/${id}/enable`, null);
  }

  /** Also revokes every token the client holds. */
  disable(id: string): Observable<ClientDetails> {
    return this.crudService.post(`${this.base}/${id}/disable`, null);
  }

  /** A new random secret, once; the old one keeps working until `previousSecretUntil`. */
  rotateSecret(id: string): Observable<RotatedSecret> {
    return this.crudService.post(`${this.base}/${id}/rotate-secret`, null);
  }

  /** Ends the grace window now. 404 when no previous secret is live. */
  revokePreviousSecret(id: string): Observable<void> {
    return this.crudService.delete(`${this.base}/${id}/previous-secret`);
  }

  /** Danger zone: every secret-holding client gets a new secret. The list is the only time they are shown. */
  rotateAll(): Observable<readonly RotatedSecret[]> {
    return this.crudService.post(`${this.base}/rotate-all`, null);
  }

  update(id: string, request: ClientDetails): Observable<ClientDetails> {
    return this.crudService.put(`${this.base}/${id}`, request);
  }

  /** Answers `void`: the new secret is only ever known to the caller that set it. */
  resetSecret(id: string, newSecret: string): Observable<void> {
    return this.crudService.post(`${this.base}/${id}/reset-secret`, {newSecret});
  }

  delete(id: string): Observable<void> {
    return this.crudService.delete(`${this.base}/${id}`);
  }

  /** The enum values a client form may offer. Cheap and static; fetch once per form. */
  options(): Observable<ClientOptions> {
    return this.crudService.get(`${this.base}/options`);
  }
}
