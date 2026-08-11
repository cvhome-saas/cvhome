import {Injectable, computed, inject, signal} from '@angular/core';
import {switchMap} from 'rxjs';
import {ApiErrorService, SelectedStoreService} from 'seller-core';
import {
  BillingInterval,
  Identifier,
  InvoiceView,
  PlanPriceView,
  PlanView,
  SubscriptionService,
  SubscriptionView,
  isOperable
} from 'seller-core/subscriptions';

@Injectable()
export class SubscriptionFacade {
  private readonly subscriptionService = inject(SubscriptionService);
  private readonly selectedStore = inject(SelectedStoreService);
  private readonly apiErrors = inject(ApiErrorService);

  readonly loading = signal<boolean>(false);
  readonly acting = signal<boolean>(false);
  readonly subscription = signal<SubscriptionView | undefined>(undefined);
  readonly plans = signal<PlanView[]>([]);
  readonly invoices = signal<InvoiceView[]>([]);
  readonly interval = signal<BillingInterval>('MONTH');

  /** The store being looked at. Every billing call is scoped to it — subscriptions belong to stores, not to orgs. */
  private store: string | undefined;

  /**
   * Whether the store has something to change. A store that has never paid has no provider subscription behind it, so
   * it must go through checkout rather than a plan change — the two are different calls and only this tells them
   * apart.
   *
   * Read from the server rather than inferred from the status, which cannot answer it: a trial we grant locally is
   * `TRIALING` with nothing at the provider, so treating that as subscribed sent "upgrade to Basic" down the
   * plan-change path and the store got a 422 for a transition that was never possible.
   */
  readonly subscribed = computed(() => this.subscription()?.providerLinked === true);

  readonly operable = computed(() => {
    const status = this.subscription()?.status;
    return status !== undefined && isOperable(status);
  });

  /**
   * Every entitlement any plan mentions, in the order the cheapest plans introduce them.
   *
   * The cards are rendered against this union rather than each plan's own keys. A plan omits a key to mean
   * *unlimited*, so rendering only what a plan lists made the most expensive one show the fewest rows — PRO looked
   * worse than BASIC precisely because it caps less. Comparing plans is the entire job of this screen.
   */
  readonly entitlementKeys = computed(() => {
    const keys: string[] = [];
    for (const plan of this.plans()) {
      for (const key of Object.keys(plan.entitlements)) {
        if (!keys.includes(key)) {
          keys.push(key);
        }
      }
    }
    return keys;
  });

  /** Plans shown at the chosen interval, cheapest tier first, skipping any not sold at that interval. */
  readonly offers = computed(() =>
    this.plans()
      .map((plan) => ({plan, price: plan.prices.find((it) => it.interval === this.interval())}))
      .filter((it): it is {plan: PlanView; price: PlanPriceView} => it.price !== undefined)
  );

  init(): void {
    this.loading.set(true);
    this.selectedStore
      .current()
      .pipe(
        switchMap((store) => {
          this.store = store;
          return this.subscriptionService.current(store);
        })
      )
      .subscribe({
        next: (subscription) => {
          this.subscription.set(subscription);
          this.interval.set(this.intervalOf(subscription));
          this.loadCatalog();
          this.loadInvoices();
        },
        error: (err) => {
          // A store billing has never seen answers 404, which is a real state rather than a fault: provisioning runs
          // through the outbox and may not have caught up. The catalog still loads so the seller can subscribe.
          this.loading.set(false);
          this.loadCatalog();
          this.apiErrors.notify(err);
        }
      });
  }

  toggleInterval(): void {
    this.interval.set(this.interval() === 'MONTH' ? 'YEAR' : 'MONTH');
  }

  isCurrent(price: PlanPriceView): boolean {
    return this.subscription()?.planPriceId?.id === price.id.id;
  }

  /**
   * Buys a plan, or moves to it.
   *
   * Which of the two is decided by whether the store already has a subscription, not by the plan being chosen — a
   * store mid-trial changes plan, while one that has never paid is sent to checkout.
   */
  choose(price: PlanPriceView): void {
    if (!this.store || this.acting()) {
      return;
    }
    this.acting.set(true);
    if (this.subscribed()) {
      this.subscriptionService.changePlan(this.store, price.id).subscribe({
        next: (subscription) => this.applied(subscription),
        error: (err) => this.failed(err)
      });
      return;
    }
    this.subscriptionService.checkout(this.store, price.id).subscribe({
      next: (session) => {
        // Left as a full-page navigation: it leaves the app for the provider's hosted page, and the store only
        // becomes active when the provider reports the money moved, not when the customer comes back.
        window.location.href = session.url;
      },
      error: (err) => this.failed(err)
    });
  }

  cancel(): void {
    this.act((store) => this.subscriptionService.cancel(store));
  }

  resume(): void {
    this.act((store) => this.subscriptionService.resume(store));
  }

  private act(call: (store: string) => ReturnType<SubscriptionService['cancel']>): void {
    if (!this.store || this.acting()) {
      return;
    }
    this.acting.set(true);
    call(this.store).subscribe({
      next: (subscription) => this.applied(subscription),
      error: (err) => this.failed(err)
    });
  }

  private applied(subscription: SubscriptionView): void {
    this.subscription.set(subscription);
    this.acting.set(false);
    this.loadInvoices();
  }

  private failed(err: unknown): void {
    this.acting.set(false);
    this.apiErrors.notify(err);
  }

  private loadCatalog(): void {
    this.subscriptionService.plans().subscribe({
      next: (plans) => {
        this.plans.set([...plans].sort((a, b) => a.tier - b.tier));
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.apiErrors.notify(err);
      }
    });
  }

  private loadInvoices(): void {
    if (!this.store) {
      return;
    }
    this.subscriptionService.invoices(this.store).subscribe({
      // Billing history is a decoration: a store that has never been charged simply has none, and failing to read it
      // should not take the page down with it.
      next: (page) => this.invoices.set(page.content ?? []),
      error: () => this.invoices.set([])
    });
  }

  private intervalOf(subscription: SubscriptionView): BillingInterval {
    const current: Identifier | null = subscription.planPriceId;
    if (!current) {
      return 'MONTH';
    }
    const match = this.plans()
      .flatMap((plan) => plan.prices)
      .find((price) => price.id.id === current.id);
    return match?.interval ?? 'MONTH';
  }
}
