import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {PlanView} from '@models/billing';

/**
 * Ported from seller-ui/projects/seller-core/subscriptions/src/lib/services/subscription.service.ts.
 *
 * The billing service, as the console sees it. Addressed at `/billing/...` through the gateway, which strips the
 * prefix and relays the session.
 *
 * Only the public catalog is ported so far. Every other call on this service is store-scoped — a subscription
 * belongs to a store rather than to an org, since one org can run a PRO store and a BASIC one side by side — and
 * belongs with the subscription-and-usage screen that needs them, not here.
 */
@Injectable({providedIn: 'root'})
export class SubscriptionService {
  private readonly crudService = inject(CrudService);

  /**
   * Every plan on sale. Public — a price list is read by people who have not signed up yet, so this is the one
   * billing call with no store and no session.
   *
   * The path is absolute where seller-core's was relative. Both resolve the same way under `<base href="/">`, but
   * a relative path silently depends on that tag; this does not.
   */
  plans(currency?: string): Observable<PlanView[]> {
    return this.crudService.get('/billing/api/v1/plan/public/plans', currency ? {currency} : undefined);
  }
}
