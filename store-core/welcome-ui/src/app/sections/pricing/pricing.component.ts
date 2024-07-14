import {Component} from '@angular/core';
import {NgClass, NgFor} from "@angular/common";
import {RouterLink} from "@angular/router";

@Component({
  selector: 'app-pricing',
  standalone: true,
  imports: [
    NgClass, NgFor, RouterLink
  ],
  templateUrl: './pricing.component.html',
  styleUrl: './pricing.component.css'
})
export class PricingComponent {
  title: string = 'Unlock Full Power Of sApp';
  desc: string = 'Lorem ipsum dolor sit amet, consectetur adipisicing elit. Laborum obcaecati dignissimos quae quo ad iste ipsum officiis deleniti asperiores sit.';
  prices: Pricing[] = [
    {
      name: "Basic",
      fade: "fadeInLeft",
      cost: "49",
      icon: "img/pricing/basic.png",
      pricingFeatures: [
        {
          desc: "5GB Linux Web Space"
        },
        {
          desc: "5 MySQL Databases"
        },
        {
          desc: "24/7 Tech Support"
        }
      ],
      url: "signup"
    },
    {
      name: "Pro",
      fade: "fadeInRight",
      cost: "49",
      icon: "img/pricing/premium.png",
      pricingFeatures: [
        {
          desc: "5GB Linux Web Space"
        },
        {
          desc: "5 MySQL Databases"
        },
        {
          desc: "24/7 Tech Support"
        }
      ],
      url: "signup"
    }
  ];
}

interface Pricing {
  name: string
  fade: string
  icon: string
  cost: string
  pricingFeatures: PricingFeature[]
  url: string
}

interface PricingFeature {
  desc: string
}
