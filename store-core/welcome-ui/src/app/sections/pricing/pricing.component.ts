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
  title: string = 'Unlock Full Power Of Cvhome';
  desc: string = 'Choose your Plan that fit your business.';
  prices: Pricing[] = [
    {
      name: "Free",
      fade: "fadeInLeft",
      cost: "0",
      icon: "img/pricing/basic.png",
      pricingFeatures: [
        {
          desc: "50 order"
        },
        {
          desc: "25 product"
        },
        {
          desc: "2 stores"
        },
        {
          desc: "2k visitor"
        },
        {
          desc: "24/7 Tech Support"
        }
      ],
      url: "signup"
    },
    {
      name: "Basic",
      fade: "fadeInDown",
      cost: "9",
      icon: "img/pricing/basic.png",
      pricingFeatures: [
        {
          desc: "200 order"
        },
        {
          desc: "100 product"
        },
        {
          desc: "5 stores"
        },
        {
          desc: "20k visitor"
        },
        {
          desc: "24/7 Tech Support"
        }
      ],
      url: "signup"
    },
    {
      name: "Performance",
      fade: "fadeInRight",
      cost: "49",
      icon: "img/pricing/premium.png",
      pricingFeatures: [
        {
          desc: "2k order"
        },
        {
          desc: "1k product"
        },
        {
          desc: "10 stores"
        },
        {
          desc: "200k visitor"
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
