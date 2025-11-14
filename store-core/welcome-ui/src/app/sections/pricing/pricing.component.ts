import {Component, Inject, OnInit, PLATFORM_ID} from '@angular/core';
import {isPlatformBrowser, NgFor, NgIf} from "@angular/common";
import {RouterLink} from "@angular/router";
import {Option, SubscriptionService, Table} from "../../service/subscription.service";

@Component({
  selector: 'app-pricing',
  standalone: true,
  imports: [
    NgFor, RouterLink, NgIf
  ],
  templateUrl: './pricing.component.html',
  styleUrl: './pricing.component.css'
})
export class PricingComponent implements OnInit {
  BASE_IMG_PATH = `img/pricing/`;
  title: string = 'Unlock Full Power Of Cvhome';
  desc: string = 'Choose your Plan that fit your business.';
  prices: Pricing[] | undefined;
  table: Table | undefined;
  freePricing: Pricing | undefined;
  flag: boolean = false;

  constructor(@Inject(PLATFORM_ID) private platformId: Object,
              private subscriptionService: SubscriptionService) {
  }

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {

      this.subscriptionService.table().subscribe(it => {
        this.table = it;
        this.constructFreePricing();
        this.displayTable();
      });
    }
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
