import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService, UI_KIT_CONFIG} from '@cvhome-saas/ui-kit';
import type {IdpPreset, IdpType} from './admin-idp.service';

/** A provider as the sign-in page draws it: a button and where it goes. */
export interface PublicIdpDto {
  readonly alias: string;
  readonly displayName: string;
  readonly preset: IdpPreset;
  readonly type: IdpType;
  /** Relative to uaa's origin: `/oauth2/authorization/<alias>`. */
  readonly authorizationUrl: string;
}

/** Why the sign-in page is being shown. */
export interface LoginContext {
  readonly clientId: string | null;
  readonly clientName: string | null;
  readonly pendingLink: {readonly providerAlias: string; readonly providerName: string; readonly email: string} | null;
}

export interface LoginSettings {
  readonly displayName: string;
  readonly defaultLocale: string;
  readonly rememberMeEnabled: boolean;
  readonly lockoutThreshold: number;
  readonly lockoutMinutes: number;
}

/**
 * What the sign-in page asks uaa before anyone is signed in, and the one call that finishes a brokered login
 * matched to an existing account. All on uaa's own origin.
 */
@Injectable({providedIn: 'root'})
export class PublicLoginService {
  private readonly crudService = inject(CrudService);
  private readonly base = inject(UI_KIT_CONFIG).uaaBasePath;

  settings(): Observable<LoginSettings> {
    return this.crudService.get(`${this.base}/api/v1/public/login/settings`);
  }

  context(): Observable<LoginContext> {
    return this.crudService.get(`${this.base}/api/v1/public/login/context`);
  }

  providers(): Observable<readonly PublicIdpDto[]> {
    return this.crudService.get(`${this.base}/api/v1/public/idps`);
  }

  /** Home-realm discovery: the provider an email's domain is sent to, or null. Never says whether an account exists. */
  discover(email: string): Observable<{provider: PublicIdpDto | null}> {
    return this.crudService.post(`${this.base}/api/v1/public/idps/discover`, {email});
  }

  /** The password step of a brokered login that matched an account; answers where to go next. */
  confirmLink(password: string): Observable<{username: string; redirectTo: string}> {
    return this.crudService.post(`${this.base}/api/v1/auth/link-confirm`, {password});
  }
}
