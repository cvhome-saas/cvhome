import {RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';
import {SubscriptionComponent} from "./subscription.component";
import {SuccessSubscriptionComponent} from "./success/success-subscription.component";
import {FailSubscriptionComponent} from "./fail/fail-subscription.component";


const routes: Routes = [{
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
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SubscriptionRoutingModule {
}
