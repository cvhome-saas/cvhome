import {Injectable, inject} from '@angular/core';
import {Observable, map} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {SpringPage} from '@core/table/table.types';
import type {CheckoutSession, Invoice, PlanView, Subscription} from '@models/billing';

const SUBSCRIPTION_API_BASE = '/billing/api/v1/subscription';

const INVOICE_API_BASE = '/billing/api/v1/invoice';

/**
 * The billing service, as the console sees it. Addressed at `/billing/...` through the gateway, which strips the
 * prefix and relays the session.
 *
 * Everything but the catalogue is **store-scoped**: a subscription belongs to a store rather than to an org, since
 * one org can run a PRO store and a BASIC one side by side. The store is passed explicitly on every one of those
 * calls rather than left to the request context, because two of them — the banner and the billing page — read a
 * store the operator is looking at, which is not always the one the context is stamping.
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

  /**
   * What this store is on, when it renews, and anything already scheduled to change.
   *
   * **404 is a real answer, not a fault.** Billing has never seen a store that has not been provisioned through
   * the quota flow, and a brand-new store can reach the console before its `StoreCreatedEvent` has been consumed.
   * Callers treat "no subscription" as a state to render, not an error to report — see `SubscriptionFacade`.
   */
  current(store: string): Observable<Subscription> {
    return this.crudService.get(`${SUBSCRIPTION_API_BASE}/current`, {store});
  }

  /**
   * Opens a hosted checkout and answers with the URL to send the browser to.
   *
   * A body with a URL rather than a 302, because a browser will not usefully follow a redirect out of an XHR.
   * The caller navigates; nothing here does, so the decision to leave the console stays at the call site.
   */
  checkout(store: string, planPriceId: string): Observable<CheckoutSession> {
    return this.crudService.post(`${SUBSCRIPTION_API_BASE}/checkout`, {planPriceId}, {store});
  }

  /**
   * Moves the store to another plan.
   *
   * One endpoint for both directions — the direction is not the caller's to choose. The answer says what actually
   * happened: an upgrade comes back already on the new plan, a downgrade comes back with `pendingPlanChange` set
   * and the old plan still in force.
   */
  changePlan(store: string, planPriceId: string): Observable<Subscription> {
    return this.crudService.post(`${SUBSCRIPTION_API_BASE}/plan`, {planPriceId}, {store});
  }

  /**
   * Switches renewal off. `immediate` ends it now and is refused for anyone but a super admin, so the console
   * never sends it — the store keeps what it paid for until the period ends.
   */
  cancel(store: string, immediate = false): Observable<Subscription> {
    return this.crudService.post(`${SUBSCRIPTION_API_BASE}/cancel`, {immediate}, {store});
  }

  /** Switches renewal back on, and calls off a scheduled downgrade if one is pending. */
  resume(store: string): Observable<Subscription> {
    return this.crudService.post(`${SUBSCRIPTION_API_BASE}/resume`, {}, {store});
  }

  /**
   * This store's invoices, newest first as billing orders them.
   *
   * The page envelope is unwrapped here: the billing page shows a short list rather than a paged table, and
   * leaking a `SpringPage` to a caller with no paging control to drive it only invites one to be invented.
   */
  invoices(store: string, size = 12): Observable<readonly Invoice[]> {
    return this.crudService
      .get<SpringPage<Invoice>>(`${INVOICE_API_BASE}/list`, {store, size})
      .pipe(map((page) => page.content ?? []));
  }
}
