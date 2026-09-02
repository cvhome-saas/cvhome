import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService, UI_KIT_CONFIG} from '@cvhome-saas/ui-kit';

/**
 * The signing keys: what exists, which one signs, and "rotate now".
 *
 * No call here ever answers key material. The JWKS endpoint serves the public halves to every
 * resource server; the private halves are encrypted at rest and never read back over HTTP.
 */
export const ADMIN_KEY_API_PATH = '/api/v1/admin/keys';

export type SigningKeyStatus = 'ACTIVE' | 'RETIRING' | 'RETIRED';

export interface SigningKeyDto {
  readonly id: string;
  readonly kid: string;
  readonly algorithm: string;
  readonly status: SigningKeyStatus;
  readonly createdAt: string;
  readonly activatedAt: string | null;
  readonly retireAfter: string | null;
  readonly retiredAt: string | null;
}

/** The posture in one read. `nextRotationAt` is null when rotation is manual (interval 0). */
export interface KeyStatus {
  readonly activeKid: string;
  readonly algorithm: string;
  readonly activatedAt: string | null;
  readonly rotationDays: number;
  readonly nextRotationAt: string | null;
  readonly retireDays: number;
  readonly retiringCount: number;
  /** Stored keys whose private half cannot be read back — the crypto provider's key changed underneath them. */
  readonly unusableCount: number;
}

@Injectable({providedIn: 'root'})
export class AdminKeyService {
  private readonly crudService = inject(CrudService);
  private readonly base = `${inject(UI_KIT_CONFIG).uaaBasePath}${ADMIN_KEY_API_PATH}`;

  list(): Observable<readonly SigningKeyDto[]> {
    return this.crudService.get(this.base);
  }

  status(): Observable<KeyStatus> {
    return this.crudService.get(`${this.base}/status`);
  }

  /** A new active key; the previous one keeps verifying for the realm's retire window. */
  rotate(): Observable<SigningKeyDto> {
    return this.crudService.post(`${this.base}/rotate`, null);
  }
}
