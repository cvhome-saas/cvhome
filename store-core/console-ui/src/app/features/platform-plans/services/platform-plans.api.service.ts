import {Injectable, inject} from '@angular/core';
import {Observable, forkJoin, map} from 'rxjs';

import {PlatformBillingService} from '@api/billing/platform-billing.service';
import {SubscriptionService} from '@api/billing/subscription.service';
import {optionalOne} from '@cvhome-saas/ui-kit';
import type {PlanView} from '@models/billing';
import type {PlanStatisticDto} from '@models/platform-billing';

/** The catalogue, and how it is actually selling. */
export interface CatalogueSnapshot {
  readonly plans: readonly PlanView[];
  /**
   * Who is on each plan and what that is worth, or null when billing could not answer.
   *
   * Null rather than an empty report: an empty one would render as zero subscribers on every row,
   * which is a claim about the business rather than about the request.
   */
  readonly statistics: PlanStatisticDto | null;
}

/**
 * The plan catalogue, in the seam every feature has.
 *
 * **Two legs, and only one of them is the page.** The catalogue is what this screen is; the plan
 * statistics fill two columns and are wrapped in `optionalOne`, so a billing aggregate that cannot
 * be read costs the subscriber count rather than the price list.
 *
 * The facade injected `SubscriptionService` straight from the api tier before this existed, which
 * made the plans screen one of the few features without the `page -> facade -> api service -> @api/*`
 * layer the rest of the console follows. The second leg is what made the seam worth having.
 */
@Injectable({providedIn: 'root'})
export class PlatformPlansApi {
  private readonly subscriptions = inject(SubscriptionService);
  private readonly platformBilling = inject(PlatformBillingService);

  loadCatalogue(): Observable<CatalogueSnapshot> {
    return forkJoin({
      // No currency: the endpoint's parameter filters the catalogue to one, and an operator wants to
      // see everything the platform sells rather than one market's slice of it.
      plans: this.subscriptions.plans(),
      statistics: this.platformBilling.planStatistics().pipe(optionalOne()),
    }).pipe(map(({plans, statistics}) => ({plans, statistics})));
  }
}
