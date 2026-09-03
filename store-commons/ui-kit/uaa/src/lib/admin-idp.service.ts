import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService, UI_KIT_CONFIG} from '@cvhome-saas/ui-kit';

/**
 * The identity providers: external logins brokered through uaa.
 *
 * A provider's `alias` is Spring's registration id and the last segment of the `redirectUri` to register
 * at the provider. The client secret is never in a response — only `hasClientSecret` — and a blank one
 * on an update keeps the stored one.
 */
export const ADMIN_IDP_API_PATH = '/api/v1/admin/identity-providers';

export type IdpType = 'OIDC' | 'OAUTH2';
export type IdpPreset = 'GOOGLE' | 'MICROSOFT' | 'APPLE' | 'GITHUB' | 'GENERIC_OIDC' | 'GENERIC_OAUTH2';
export type AccountLinking = 'LINK' | 'CONFIRM' | 'REJECT';

export interface IdentityProviderDto {
  readonly id: string;
  readonly alias: string;
  readonly displayName: string;
  readonly type: IdpType;
  readonly preset: IdpPreset;
  readonly enabled: boolean;
  readonly hideOnLogin: boolean;
  readonly sortOrder: number;
  readonly clientId: string;
  readonly hasClientSecret: boolean;
  readonly issuerUri: string | null;
  readonly authorizationUri: string | null;
  readonly tokenUri: string | null;
  readonly userInfoUri: string | null;
  readonly jwkSetUri: string | null;
  readonly scopes: readonly string[];
  readonly userNameAttribute: string | null;
  readonly clientAuthMethod: string;
  readonly emailDomains: readonly string[];
  readonly accountLinking: AccountLinking;
  readonly jitProvisioning: boolean;
  readonly defaultRoles: readonly string[];
  readonly trustEmailVerified: boolean;
  readonly attributeMapping: Readonly<Record<string, string>>;
  /** What to register at the provider. */
  readonly redirectUri: string;
  readonly createdAt: string;
  readonly updatedAt: string;
}

/** What creates or updates one. Blank endpoints take the preset's defaults; `preset` is fixed after creation. */
export interface IdentityProviderRequest {
  readonly alias: string;
  readonly displayName: string | null;
  readonly preset: IdpPreset;
  readonly hideOnLogin: boolean;
  readonly clientId: string;
  readonly clientSecret: string | null;
  readonly issuerUri: string | null;
  readonly authorizationUri: string | null;
  readonly tokenUri: string | null;
  readonly userInfoUri: string | null;
  readonly jwkSetUri: string | null;
  readonly scopes: readonly string[];
  readonly userNameAttribute: string | null;
  readonly clientAuthMethod: string | null;
  readonly emailDomains: readonly string[];
  readonly accountLinking: AccountLinking;
  readonly jitProvisioning: boolean;
  readonly defaultRoles: readonly string[];
  readonly trustEmailVerified: boolean;
  readonly attributeMapping: Readonly<Record<string, string>>;
}

/** One entry of the type chooser. */
export interface IdpPresetDto {
  readonly preset: IdpPreset;
  readonly type: IdpType;
  readonly displayName: string;
  readonly generic: boolean;
  readonly needsIssuer: boolean;
  readonly needsEndpoints: boolean;
  readonly defaultScopes: readonly string[];
  readonly defaultMapping: Readonly<Record<string, string>>;
  /** False for Apple, which ships scaffolded and untested against a developer account. */
  readonly verified: boolean;
}

export interface IdpTestResult {
  readonly ok: boolean;
  readonly checked: string;
  readonly discoveredIssuer: string | null;
  readonly detail: string;
}

/** A linked external identity on an account. */
export interface UserIdentityDto {
  readonly id: string;
  readonly providerAlias: string | null;
  readonly providerName: string | null;
  readonly subject: string;
  readonly email: string | null;
  readonly linkedAt: string;
  readonly lastLoginAt: string | null;
}

@Injectable({providedIn: 'root'})
export class AdminIdpService {
  private readonly crudService = inject(CrudService);
  private readonly base = `${inject(UI_KIT_CONFIG).uaaBasePath}${ADMIN_IDP_API_PATH}`;

  list(): Observable<readonly IdentityProviderDto[]> {
    return this.crudService.get(this.base);
  }

  presets(): Observable<readonly IdpPresetDto[]> {
    return this.crudService.get(`${this.base}/presets`);
  }

  findOne(id: string): Observable<IdentityProviderDto> {
    return this.crudService.get(`${this.base}/${id}`);
  }

  create(request: IdentityProviderRequest): Observable<IdentityProviderDto> {
    return this.crudService.post(this.base, request);
  }

  update(id: string, request: IdentityProviderRequest): Observable<IdentityProviderDto> {
    return this.crudService.put(`${this.base}/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.crudService.delete(`${this.base}/${id}`);
  }

  enable(id: string): Observable<IdentityProviderDto> {
    return this.crudService.post(`${this.base}/${id}/enable`, null);
  }

  disable(id: string): Observable<IdentityProviderDto> {
    return this.crudService.post(`${this.base}/${id}/disable`, null);
  }

  /** Reaches the provider; a 502 carries the provider and its status. */
  test(id: string): Observable<IdpTestResult> {
    return this.crudService.post(`${this.base}/${id}/test`, null);
  }

  /** The sign-in page's order: a bare array of aliases. */
  reorder(aliases: readonly string[]): Observable<readonly IdentityProviderDto[]> {
    return this.crudService.put(`${this.base}/order`, aliases);
  }
}
