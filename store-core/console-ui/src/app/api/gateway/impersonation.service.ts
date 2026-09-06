import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService, type ImpersonationState} from '@cvhome-saas/ui-kit';
import type {StartImpersonation} from '@models/impersonation';

/**
 * Acting as a merchant — the gateway's endpoint, not a backend's.
 *
 * The console never holds a token: the gateway does, in the session, and `POST` swaps it for one that
 * is the merchant's. So the endpoint lives on the gateway's own origin (`/api/v1/…`, beside
 * `auth/me`) rather than behind a service prefix, and the request context's `?store=` rides along
 * harmlessly — the gateway reads the body, not the query.
 *
 * `DELETE` is idempotent: a session that is already itself answers the same 204.
 */
const IMPERSONATION = '/api/v1/impersonation';

@Injectable({providedIn: 'root'})
export class ImpersonationService {
  private readonly crudService = inject(CrudService);

  start(request: StartImpersonation): Observable<ImpersonationState> {
    return this.crudService.post<ImpersonationState, StartImpersonation>(IMPERSONATION, request);
  }

  end(): Observable<void> {
    return this.crudService.delete<void>(IMPERSONATION);
  }
}
