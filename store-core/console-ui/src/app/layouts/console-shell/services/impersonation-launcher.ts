import {DOCUMENT} from '@angular/common';
import {Injectable, inject} from '@angular/core';
import {Observable, map} from 'rxjs';

import {ImpersonationService} from '@api/gateway/impersonation.service';
import {SelectedStoreService} from '@api/tenancy/selected-store.service';
import type {StartImpersonation} from '@models/impersonation';

/**
 * Starts acting as a merchant, then leaves for their dashboard.
 *
 * **A full reload, on purpose.** Identity, rail, store list and every page facade keyed on the
 * selected store change at once when the gateway swaps the session, and the app has no global
 * invalidation for that; a reload is honest where a cascade of signals would be a guess. The store
 * the operator chose is written as the selection first, so the dashboard opens on it rather than on
 * whichever of the merchant's stores sorts first.
 *
 * Shared by the two screens that offer the action, so they agree on where an impersonation lands.
 * In `layouts/` rather than `shared/` because it reaches the api tier, which `shared/` may not.
 */
@Injectable({providedIn: 'root'})
export class ImpersonationLauncher {
  private readonly impersonation = inject(ImpersonationService);
  private readonly selection = inject(SelectedStoreService);
  private readonly document = inject(DOCUMENT);

  /** Resolves once the gateway has swapped the session; the page is already on its way out. */
  start(request: StartImpersonation): Observable<void> {
    return this.impersonation.start(request).pipe(
      map(() => {
        this.selection.invalidate();
        this.selection.selectStore(request.storeId);
        this.document.defaultView?.location.assign('/dashboard');
      }),
    );
  }
}
