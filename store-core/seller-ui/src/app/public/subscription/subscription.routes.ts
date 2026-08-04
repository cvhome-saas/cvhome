import {Routes} from '@angular/router';
import {SubscriptionComponent} from './subscription.component';
import {SuccessSubscriptionComponent} from './success/success-subscription.component';
import {FailSubscriptionComponent} from './fail/fail-subscription.component';

export const SUBSCRIPTION_ROUTES: Routes = [
  {
    path: '',
    component: SubscriptionComponent,
    children: [
      {
        path: 'success',
        component: SuccessSubscriptionComponent,
      },
      {
        path: 'fail',
        component: FailSubscriptionComponent,
      },
    ],
  }
];
