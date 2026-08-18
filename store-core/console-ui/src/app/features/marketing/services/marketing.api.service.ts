import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {SubscriptionService} from '@api/billing/subscription.service';
import type {PlanView} from '@models/billing';

@Injectable({providedIn: 'root'})
export class MarketingApi {
  private readonly subscriptions = inject(SubscriptionService);

  /**
   * The public plan catalog.
   *
   * Handed over unshaped, and fetched once: the response carries every interval a plan sells at, so re-fetching on
   * the monthly/yearly toggle would ask the server for what it already said. The facade re-shapes it instead.
   */
  plans(): Observable<PlanView[]> {
    return this.subscriptions.plans();
  }
}
