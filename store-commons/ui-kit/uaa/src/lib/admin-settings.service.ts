import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService, UI_KIT_CONFIG} from '@cvhome-saas/ui-kit';

/**
 * The realm's policy: one document, read whole, written whole with its `version`.
 *
 * Behind the same `SCOPE_super_admin`/`ROLE_SUPER_ADMIN` gate as the rest of `/api/v1/admin/**`.
 * A write with a stale `version` is a 409 `UAA.SETTINGS.CONFLICT`; a value out of range is a 400
 * `UAA.SETTINGS.INVALID` with the field named.
 */
export const ADMIN_SETTINGS_API_PATH = '/api/v1/admin/settings';

export interface PasswordPolicy {
  readonly minLength: number;
  readonly requireUpper: boolean;
  readonly requireLower: boolean;
  readonly requireDigit: boolean;
  readonly requireSpecial: boolean;
  readonly historyCount: number;
  /** 0 = never. */
  readonly expiryDays: number;
  readonly rejectBreached: boolean;
}

export interface LockoutPolicy {
  readonly threshold: number;
  readonly durationSeconds: number;
  /** 0 = never permanent. */
  readonly permanentAfter: number;
}

export interface SessionPolicy {
  readonly idleSeconds: number;
  readonly maxSeconds: number;
  readonly rememberMeEnabled: boolean;
  readonly rememberMeSeconds: number;
  readonly singleSessionPerUser: boolean;
}

export interface TokenPolicy {
  readonly maxAccessTokenTtlSeconds: number;
  readonly defaultAccessTokenTtlSeconds: number;
  readonly defaultRefreshTokenTtlSeconds: number;
  /** 0 = never expires. */
  readonly clientSecretValidityDays: number;
  readonly clientSecretGraceHours: number;
}

export interface KeyPolicy {
  /** 0 = manual only. */
  readonly rotationDays: number;
  readonly retireDays: number;
}

export interface RealmSettings {
  readonly displayName: string;
  readonly supportEmail: string | null;
  readonly defaultLocale: string;
  readonly selfRegistrationEnabled: boolean;
  readonly requireEmailVerification: boolean;
  readonly password: PasswordPolicy;
  readonly lockout: LockoutPolicy;
  readonly sessions: SessionPolicy;
  readonly tokens: TokenPolicy;
  readonly keys: KeyPolicy;
  readonly auditRetentionDays: number;
  readonly updatedAt: string | null;
  readonly updatedBy: string | null;
  readonly version: number;
}

@Injectable({providedIn: 'root'})
export class AdminSettingsService {
  private readonly crudService = inject(CrudService);
  private readonly base = `${inject(UI_KIT_CONFIG).uaaBasePath}${ADMIN_SETTINGS_API_PATH}`;

  get(): Observable<RealmSettings> {
    return this.crudService.get(this.base);
  }

  update(settings: RealmSettings): Observable<RealmSettings> {
    return this.crudService.put(this.base, settings);
  }
}
