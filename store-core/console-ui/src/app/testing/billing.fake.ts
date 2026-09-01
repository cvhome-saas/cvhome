import {Observable, of} from 'rxjs';

import {ApiError} from '@cvhome-saas/ui-kit';
import type {CheckoutSession, Invoice, PlanView, Subscription} from '@models/billing';

/**
 * A `SubscriptionService` that answers without HTTP.
 *
 * The console shell mounts `PlanBanner`, which reads `SubscriptionFacade`, which reads this service — so
 * every spec that renders the shell now needs billing to be constructible. Providing this is one line
 * and keeps those specs about the shell; wiring `provideHttpClientTesting` instead would drag in the
 * request context and the whole interceptor chain for a call most of them never make.
 *
 * Defaults to "billing has never seen this store": `current` answers a 404-shaped error, which
 * `SubscriptionFacade` treats as a state rather than a failure. Set `subscription` for a spec that needs a
 * live one.
 */
export class FakeSubscriptionService {
  subscription: Subscription | null = null;
  invoiceList: readonly Invoice[] = [];
  planList: PlanView[] = [];

  plans(): Observable<PlanView[]> {
    return of(this.planList);
  }

  current(_store?: string): Observable<Subscription> {
    if (!this.subscription) {
      /*
       * A real `ApiError`, not a plain object with a status: `SubscriptionFacade` narrows on
       * `instanceof ApiError` before treating a 404 as "no subscription", so a look-alike would take
       * the rethrow path and leave the resource in error — which is exactly the wrong state to be
       * testing against.
       */
      return new Observable<Subscription>((subscriber) =>
        subscriber.error(
          new ApiError({code: 'CONTROL_PLANE.SUBSCRIPTION.NOT_FOUND', category: 'NOT_FOUND', status: 404}),
        ),
      );
    }
    return of(this.subscription);
  }

  /*
   * These mirror the real signatures rather than taking nothing, so a spec can assert *what* was
   * asked for — `toHaveBeenCalledWith(store, priceId)` — and a drift in the service's parameters is a
   * compile error here instead of a silently weaker test.
   */
  checkout(_store: string, _planPriceId: string): Observable<CheckoutSession> {
    return of({url: 'https://checkout.test/session'});
  }

  changePlan(_store: string, _planPriceId: string): Observable<Subscription> {
    return this.current();
  }

  cancel(_store: string, _immediate = false): Observable<Subscription> {
    return this.current();
  }

  resume(_store: string): Observable<Subscription> {
    return this.current();
  }

  invoices(_store: string, _size?: number): Observable<readonly Invoice[]> {
    return of(this.invoiceList);
  }
}

/** A healthy paid subscription — the state that produces no banner at all. */
export function activeSubscription(): Subscription {
  return {
    store: 'store-1',
    status: 'ACTIVE',
    planCode: 'PRO',
    planDisplayName: 'Pro',
    planPriceId: 'price-pro-m',
    amount: {currency: {code: 'USD'}, minorUnits: 3000},
    currentPeriodEnd: new Date(Date.now() + 30 * 86_400_000).toISOString(),
    trialEnd: null,
    cancelAtPeriodEnd: false,
    graceUntil: null,
    pendingPlanChange: null,
    providerLinked: true,
    entitlements: {},
  };
}

/** A trial with a week to run — close enough to the end that the banner counts it down. */
export function trialingSubscription(): Subscription {
  return {
    ...activeSubscription(),
    status: 'TRIALING',
    trialEnd: new Date(Date.now() + 7 * 86_400_000).toISOString(),
    providerLinked: false,
  };
}
