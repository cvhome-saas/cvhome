import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService, UI_KIT_CONFIG} from '@cvhome-saas/ui-kit';

import type {SessionSummary} from './uaa.models';

/**
 * What a signed-in person may do to their own account: change the password, see and end their sessions.
 *
 * Any authenticated user; a service client is refused as not a user (403 `UAA.AUTH.NOT_A_USER_PRINCIPAL`).
 * A password change ends every other session and every token of the account — that is what a person does after a
 * scare, and the console says so before they press the button.
 */
export const ACCOUNT_API_PATH = '/api/v1/account';

export interface ChangePasswordRequest {
  readonly currentPassword: string;
  readonly newPassword: string;
}

@Injectable({providedIn: 'root'})
export class AccountService {
  private readonly crudService = inject(CrudService);
  private readonly base = `${inject(UI_KIT_CONFIG).uaaBasePath}${ACCOUNT_API_PATH}`;

  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.crudService.put(`${this.base}/password`, request);
  }

  sessions(): Observable<readonly SessionSummary[]> {
    return this.crudService.get(`${this.base}/sessions`);
  }

  revokeSession(sessionId: string): Observable<void> {
    return this.crudService.delete(`${this.base}/sessions/${sessionId}`);
  }

  /** Ends every session but this one. */
  revokeOtherSessions(): Observable<{revoked: number}> {
    return this.crudService.delete(`${this.base}/sessions`);
  }
}
