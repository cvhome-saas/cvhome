import {Injectable, PLATFORM_ID, inject, signal} from '@angular/core';
import {isPlatformBrowser} from '@angular/common';
import {PlanView, SubscriptionService} from 'seller-core/subscriptions';
import {Pricing, toPricing} from '../mappers/pricing.mapper';

@Injectable()
export class PricingFacade {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly subscriptionService = inject(SubscriptionService);

  private plans: PlanView[] = [];

  readonly prices = signal<Pricing[] | undefined>(undefined);
  readonly freePricing = signal<Pricing | undefined>(undefined);
  readonly isYearly = signal<boolean>(false);
  readonly hasPlans = signal<boolean>(false);

  init(): void {
    // Server-side rendering skips this: the catalog is a browser-side fetch and the page is useful without it.
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    this.subscriptionService.plans().subscribe((plans) => {
      this.plans = [...plans].sort((a, b) => a.tier - b.tier);
      this.hasPlans.set(this.plans.length > 0);
      this.updatePrices();
    });
  }

  toggle(): void {
    this.isYearly.set(!this.isYearly());
    this.updatePrices();
  }

  private updatePrices(): void {
    const interval = this.isYearly() ? 'YEAR' : 'MONTH';
    const priced = this.plans
      .map((plan) => ({plan, price: plan.prices.find((it) => it.interval === interval)}))
      .filter((it) => it.price !== undefined);

    // The free plan is shown on its own below the paid ones, the way this page has always laid out. It is whichever
    // plan costs nothing rather than one called "FREE", so a catalog that renames it keeps working.
    const free = priced.find((it) => it.price!.amount.minorUnits === 0);
    this.freePricing.set(free ? toPricing(free.plan, free.price!) : undefined);
    this.prices.set(priced.filter((it) => it !== free).map((it) => toPricing(it.plan, it.price!)));
  }
}
