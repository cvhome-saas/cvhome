import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@cvhome-saas/ui-kit';
import type {CreateOrgRequest, ReadableUser} from '@models/signup';

export const SIGNUP_API_BASE = '/tenancy/api/v1/signup/public';

@Injectable({providedIn: 'root'})
export class SignUpService {
  private readonly crudService = inject(CrudService);

  /**
   * Creates the organization and its first administrator, or neither — the server holds both in one transaction.
   *
   * Conflicts (`UaaConflictException`, e.g. the email is already registered) arrive as an RFC-7807 problem with a
   * `fieldErrors[]` entry, so callers should hand the error to `ApiErrorService.applyToForm` rather than toasting it.
   */
  signUp(request: CreateOrgRequest): Observable<ReadableUser> {
    return this.crudService.post(`${SIGNUP_API_BASE}/create`, request);
  }
}
