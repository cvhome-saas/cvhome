import {Routes} from '@angular/router';
import {SubscriptionAndUsageComponent} from './subscription-and-usage.component';
import {SubscriptionComponent} from './subscription/subscription.component';
import {UsageComponent} from './usage/usage.component';

export const SUBSCRIPTION_AND_USAGE_ROUTES: Routes = [
  {
    path: '', component: SubscriptionAndUsageComponent, children: [
      {
        path: 'subscription',
        component: SubscriptionComponent,
      },
      {
        path: 'usage',
        component: UsageComponent,
      }
    ],
  }
];
