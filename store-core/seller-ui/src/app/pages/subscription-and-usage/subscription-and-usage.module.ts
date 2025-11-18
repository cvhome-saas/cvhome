import {NgModule} from '@angular/core';
import {NbAlertModule, NbDialogModule, NbToggleModule} from '@nebular/theme';
import {SharedModule} from "../../shared/shared.module";
import {NgxFileDropModule} from "ngx-file-drop";
import {SubscriptionAndUsageRoutingModule} from "./subscription-and-usage-routing.module";
import {SubscriptionAndUsageComponent} from "./subscription-and-usage.component";
import {SubscriptionComponent} from "./subscription/subscription.component";
import {UsageComponent} from "./usage/usage.component";

@NgModule({
  declarations: [
    SubscriptionAndUsageComponent,
    SubscriptionComponent,
    UsageComponent
  ],
  imports: [
    SubscriptionAndUsageRoutingModule,
    SharedModule,
    NbDialogModule.forChild(),
    NgxFileDropModule,
    NbToggleModule,
    NbAlertModule,
  ]
})
export class SubscriptionAndUsageModule {
}
