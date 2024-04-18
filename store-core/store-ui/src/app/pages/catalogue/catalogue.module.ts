import { NgModule } from '@angular/core';

import { CatalogueRoutingModule } from './catalogue-routing.module';
import { CatalogueComponent } from './catalogue.component';
import { NbDialogModule } from '@nebular/theme';
import {SharedModule} from "../store-manager/shared/shared.module";

@NgModule({
  declarations: [
    CatalogueComponent
  ],
  imports: [
    CatalogueRoutingModule,
    NbDialogModule.forChild(),
    SharedModule
  ]
})
export class CatalogueModule {
}
