import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {SubscriptionService} from '@api/billing/subscription.service';
import type {CheckoutSession, Subscription} from '@models/billing';

/**
 * The billing page's calls, in the seam every other feature has.
 *
 * The page's facade used to inject `SubscriptionService` straight from the api tier, which made
 * billing one of two features without the `page -> facade -> api service -> @api/*` layer the rest
 * of the console follows. Nothing here reshapes much — billing's DTOs are already the shape the
 * page wants — but the seam is where a mapper goes when one is needed, and where a spec can stand
 * in without faking HTTP.
 *
 * Reading the subscription is deliberately *not* here: it belongs to `SubscriptionFacade`, which the
 * shell's plan banner reads too, and fetching it twice is how the banner and the page came to
 * disagree in the first place.
 */
@Injectable({providedIn: 'root'})
export class BillingApi {
  private readonly subscriptions = inject(SubscriptionService);

  /** Starts a hosted checkout for a store with no card on file. */
  checkout(store: string, planPriceId: string): Observable<CheckoutSession> {
    return this.subscriptions.checkout(store, planPriceId);
  }

  /** Moves a store already paying to another plan, prorated against the card on file. */
  changePlan(store: string, planPriceId: string): Observable<Subscription> {
    return this.subscriptions.changePlan(store, planPriceId);
  }

  /** Turns renewal back on for a subscription set to lapse. */
  resume(store: string): Observable<Subscription> {
    return this.subscriptions.resume(store);
  }

  /**
   * Schedules a cancellation for the end of the period.
   *
   * `false` is not a default worth hiding: immediate cancellation is a super-admin operation and is
   * never sent from the console, so this method does not offer it.
   */
  cancelAtPeriodEnd(store: string): Observable<Subscription> {
    return this.subscriptions.cancel(store, false);
  }
}
