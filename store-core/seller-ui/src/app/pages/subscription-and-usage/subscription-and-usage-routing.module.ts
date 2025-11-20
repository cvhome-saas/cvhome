import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {UsageComponent} from "./usage/usage.component";
import {SubscriptionAndUsageComponent} from "./subscription-and-usage.component";
import {SubscriptionComponent} from "./subscription/subscription.component";

const routes: Routes = [
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

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SubscriptionAndUsageRoutingModule {
}
