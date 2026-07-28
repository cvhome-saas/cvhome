import {Injectable, inject, signal} from '@angular/core';
import {zip} from 'rxjs';
import {SubscriptionDetails, SubscriptionService, Table} from '../services/subscription.service';
import {ErrorService} from '../../shared/services/error.service';
import {PRICING_CARD_ACCENTS} from '../constants/subscription.constants';
import {Pricing, toFreePricing, toPricing} from '../mappers/subscription.mapper';

@Injectable()
export class SubscriptionFacade {
  private readonly subscriptionService = inject(SubscriptionService);
  private readonly errorService = inject(ErrorService);

  readonly loading = signal<boolean>(false);
  readonly currentSubscriptionDetails = signal<SubscriptionDetails | undefined>(undefined);
  readonly prices = signal<Pricing[] | undefined>(undefined);
  readonly freePricing = signal<Pricing | undefined>(undefined);
  readonly isYearly = signal<boolean>(false);
  readonly accents = PRICING_CARD_ACCENTS;

  private table: Table | undefined;

  init(): void {
    this.loading.set(true);
    zip(this.subscriptionService.details(), this.subscriptionService.table()).subscribe({
      next: ([details, table]) => {
        this.currentSubscriptionDetails.set(details);
        this.isYearly.set(details.recurringPlan === 'YEAR');
        this.table = table;
        this.freePricing.set(toFreePricing(table.freeOption));
        this.updatePrices();
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
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
