import {Component, OnInit} from '@angular/core';
import {Option, SubscriptionDetails, SubscriptionService, Table} from "../service/subscription.service";
import {zip} from "rxjs";
import {ErrorService} from "../../../shared/services/error.service";

@Component({
  selector: 'ngx-subscription',
  standalone: false,
  templateUrl: './subscription.component.html',
  styleUrls: ['./subscription.component.scss']
})
export class SubscriptionComponent implements OnInit {
  loader = false;
  BASE_IMG_PATH = `assets/img/pricing/`;

  prices: Pricing[] | undefined;
  accent: ("" | "basic" | "primary" | "success" | "warning" | "danger" | "info" | "control")[] = ["success", "primary", "danger", "warning", "basic", "info"];
  table: Table | undefined;
  currentSubscriptionDetails: SubscriptionDetails | undefined;
  freePricing: Pricing | undefined;
  flag: boolean = false;

  constructor(private subscriptionService: SubscriptionService, private errorService: ErrorService) {
  }

  ngOnInit(): void {
    zip(this.subscriptionService.details(), this.subscriptionService.table()).subscribe({
        next: (it) => {
          this.currentSubscriptionDetails = it[0];
          this.flag = (this.currentSubscriptionDetails.recurringPlan == 'YEAR')
          this.table = it[1];
          this.constructFreePricing();
          this.displayTable();
        },
        error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      }
    )
  }

  toggle() {
    this.flag = !this.flag;
    this.displayTable();
  }

  constructFreePricing() {
    if (this.table) {
      this.freePricing = {
        name: this.table.freeOption.subscriptionPlan,
        cost: `${this.table.freeOption.cost.price / 100}`,
        url: "",
        icon: `${this.BASE_IMG_PATH}${this.table.freeOption.subscriptionPlan.toLowerCase()}.png`,
        pricingFeatures: this.table.freeOption.feature.features.map(it => {
          return {desc: it.code} as PricingFeature
        }),
      } as Pricing;
    }
  }

  displayTable() {
    this.prices = [];
    let options: Option[] | undefined;
    if (!this.flag) {
      options = this.table?.tables.MONTH.options;
    } else {
      options = this.table?.tables.YEAR.options;
    }
    if (options && options.length > 0) {

      this.prices = options.map(it => {
        return {
          id: it.id,
          name: it.subscriptionPlan,
          cost: `${it.cost.price / 100}`,
          previousCost: `${it.previousCost.price / 100}`,
          url: "",
          icon: `${this.BASE_IMG_PATH}${it.subscriptionPlan.toLowerCase()}.png`,
          pricingFeatures: it.feature.features.map(it => {
            return {desc: it.code} as PricingFeature
          }),
        } as Pricing
      })
    }
  }

}

interface Pricing {
  id: PriceId
  name: string
  icon: string
  cost: string
  previousCost: string
  pricingFeatures: PricingFeature[]
  url: string
}

interface PriceId {
  id: string
}

interface PricingFeature {
  desc: string
}

