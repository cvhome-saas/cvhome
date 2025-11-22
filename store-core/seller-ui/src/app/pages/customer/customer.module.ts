import {NgModule} from '@angular/core';
import {CustomersRoutingModule, routedComponents} from './customer-routing.module';
import {NbDialogModule} from '@nebular/theme';
import {SharedModule} from "../shared/shared.module";

@NgModule({
  declarations: [
    ...routedComponents
  ],
  imports: [
    CustomersRoutingModule,
    SharedModule,
    NbDialogModule.forChild()
  ],
  exports: []
})
export class CustomersModule {
}
