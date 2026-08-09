import {Injectable, PLATFORM_ID, inject, signal} from '@angular/core';
import {isPlatformBrowser} from '@angular/common';
import {SubscriptionService} from 'seller-core/signup';
import {Table} from 'seller-core';
import {Pricing, toFreePricing, toPricing} from '../mappers/pricing.mapper';

@Injectable()
export class PricingFacade {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly subscriptionService = inject(SubscriptionService);

  private table: Table | undefined;

  readonly prices = signal<Pricing[] | undefined>(undefined);
  readonly freePricing = signal<Pricing | undefined>(undefined);
  readonly isYearly = signal<boolean>(false);
  readonly hasPlans = signal<boolean>(false);

  init(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    this.subscriptionService.table().subscribe((table) => {
      this.table = table;
      this.hasPlans.set(!!(table.tables.YEAR || table.tables.MONTH));
      this.freePricing.set(toFreePricing(table.freeOption));
      this.updatePrices();
    });
  }

  toggle(): void {
    this.isYearly.set(!this.isYearly());
    this.updatePrices();
  }

  private updatePrices(): void {
    const options = this.isYearly() ? this.table?.tables.YEAR.options : this.table?.tables.MONTH.options;
    this.prices.set(options && options.length > 0 ? options.map(toPricing) : []);
  }
}
