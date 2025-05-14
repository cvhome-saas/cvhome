import {NgModule} from '@angular/core';
import {PublicRoutingModule} from './public-routing.module';
import {PublicComponent} from "./public.component";
import {ThemeModule} from '../theme/theme.module';
import {SharedModule} from "../shared/shared.module";
import {SubscriptionModule} from "./subscription/subscription.module";


@NgModule({
  declarations: [PublicComponent],
  imports: [
    PublicRoutingModule,
    ThemeModule,
    SharedModule,
    SubscriptionModule
  ]
})
export class PublicModule {
}
