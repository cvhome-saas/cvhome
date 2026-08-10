import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';
import {CrudService} from 'seller-core';
import {
  CheckoutSessionView,
  Identifier,
  InvoiceView,
  Page,
  PlanView,
  SubscriptionView
} from '../models/billing.model';

/**
 * The billing service, as the seller console sees it.
 *
 * Every call but the catalog is scoped to a store, because a subscription belongs to a store rather than to the org:
 * one org can run a PRO store and a BASIC one side by side, and the console is always looking at exactly one of them.
 *
 * Addressed at `billing/...` through the gateway, which strips the prefix and relays the session.
 */
@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private readonly crudService = inject(CrudService);

  /**
   * Every plan on sale. Public — a price list is read by people who have not signed up yet, so this is the one
   * billing call with no store and no session.
   */
  plans(currency?: string): Observable<PlanView[]> {
    return this.crudService.get('billing/api/v1/plan/public/plans', currency ? {currency} : undefined);
  }

  current(store: string): Observable<SubscriptionView> {
    return this.crudService.get('billing/api/v1/subscription/current', {store});
  }

  invoices(store: string, page = 0, count = 20): Observable<Page<InvoiceView>> {
    return this.crudService.get('billing/api/v1/invoice/list', {store, page, count});
  }

  /**
   * Opens a hosted checkout for a store that is not paying yet, and answers with where to send the customer.
   *
   * Returns a URL rather than redirecting: the console opens it itself, which a browser will not do usefully for a
   * redirect out of an XHR.
   */
  checkout(store: string, planPriceId: Identifier): Observable<CheckoutSessionView> {
    return this.crudService.post('billing/api/v1/subscription/checkout', {planPriceId}, {store});
  }

  /**
   * Moves an already-paying store to another plan.
   *
   * There is no separate upgrade and downgrade call, and that is the server's decision to make: moving up is charged
   * and applied at once, moving down waits for the period already paid for. A console that chose would eventually
   * choose wrong.
   */
  changePlan(store: string, planPriceId: Identifier): Observable<SubscriptionView> {
    return this.crudService.post('billing/api/v1/subscription/plan', {planPriceId}, {store});
  }

  /** Switches renewal off. The store keeps working until the period it has paid for runs out. */
  cancel(store: string): Observable<SubscriptionView> {
    return this.crudService.post('billing/api/v1/subscription/cancel', {immediate: false}, {store});
  }

  /** Switches renewal back on, and calls off a pending downgrade with it. */
  resume(store: string): Observable<SubscriptionView> {
    return this.crudService.post('billing/api/v1/subscription/resume', null, {store});
  }
}
