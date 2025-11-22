import {NgModule} from '@angular/core';
import {SubscriptionRoutingModule} from "./subscription-routing.module";
import {SubscriptionComponent} from "./subscription.component";
import {SuccessSubscriptionComponent} from "./success/success-subscription.component";
import {FailSubscriptionComponent} from "./fail/fail-subscription.component";
import {SharedModule} from "../../pages/shared/shared.module";

@NgModule({
    exports: [],
    imports: [
        SubscriptionRoutingModule,
        SharedModule
    ],
    declarations: [
        SubscriptionComponent,
        SuccessSubscriptionComponent,
        FailSubscriptionComponent
    ],
})
export class SubscriptionModule {
}
