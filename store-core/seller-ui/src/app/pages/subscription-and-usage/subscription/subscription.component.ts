import {DatePipe} from '@angular/common';
import {Component, OnInit, inject} from '@angular/core';
import {
  NbAlertModule,
  NbButtonModule,
  NbCardModule,
  NbSpinnerModule,
  NbComponentStatus,
  NbTagModule,
  NbToggleModule
} from '@nebular/theme';
import {TranslateModule} from '@ngx-translate/core';
import {PRICING_CARD_ACCENTS, PricingCardAccent} from 'seller-core/subscriptions';
import {Money, PlanView, SubscriptionStatus, formatAmount} from 'seller-core/subscriptions';
import {SubscriptionFacade} from '../facades/subscription.facade';

const UNLIMITED = '\u221e';
const GRANTED = '\u2713';
const WITHHELD = '\u2014';

/** One entitlement as a plan card shows it. */
interface EntitlementRow {
  key: string;
  display: string;
}

@Component({
  selector: 'ngx-subscription',
  standalone: true,
  imports: [
    NbCardModule,
    NbToggleModule,
    NbAlertModule,
    NbSpinnerModule,
    NbButtonModule,
    NbTagModule,
    TranslateModule,
    DatePipe
  ],
  templateUrl: './subscription.component.html',
  styleUrls: ['./subscription.component.scss'],
  providers: [SubscriptionFacade]
})
export class SubscriptionComponent implements OnInit {
  protected readonly facade = inject(SubscriptionFacade);

  ngOnInit(): void {
    this.facade.init();
  }

  protected accentFor(index: number): PricingCardAccent {
    return PRICING_CARD_ACCENTS[index % PRICING_CARD_ACCENTS.length];
  }

  protected amount(money: Money | null): string {
    return formatAmount(money);
  }

  /**
   * Colours the status chip by what the seller has to do about it: nothing, watch it, or act now.
   */
  protected statusStatus(status: SubscriptionStatus): NbComponentStatus {
    switch (status) {
      case 'ACTIVE':
        return 'success';
      case 'TRIALING':
        return 'info';
      case 'PAST_DUE':
        return 'warning';
      case 'SUSPENDED':
      case 'CANCELED':
        return 'danger';
      default:
        return 'basic';
    }
  }

  /**
   * A plan's entitlements as display rows, one per key any plan in the catalog mentions.
   *
   * A key a plan omits means *unlimited*, which is the same rule the server enforces — so it renders as such rather
   * than as a missing row. Rendering only the keys a plan happens to list made the most expensive plan show the
   * fewest lines, since capping less is exactly what it is selling.
   */
  protected entitlementsOf(plan: PlanView): EntitlementRow[] {
    return this.facade.entitlementKeys().map((key) => {
      const value = plan.entitlements[key];
      if (!value) {
        return {key, display: UNLIMITED};
      }
      if (value.flagValue !== null) {
        return {key, display: value.flagValue ? GRANTED : WITHHELD};
      }
      return {key, display: value.limitValue === null ? UNLIMITED : `${value.limitValue}`};
    });
  }
}
