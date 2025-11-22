import {NgModule} from '@angular/core';
import {ContentRoutingModule, routedComponents} from './content-routing.module';
import {NbDialogModule} from '@nebular/theme';
import {LightboxModule} from 'ngx-lightbox';
import {SharedModule} from "../shared/shared.module";
import {NgOptimizedImage} from "@angular/common";
import {NgxFileDropModule} from "ngx-file-drop";

@NgModule({
  declarations: [
    ...routedComponents
  ],
  imports: [
    ContentRoutingModule,
    SharedModule,
    NbDialogModule.forChild(),
    LightboxModule,
    NgOptimizedImage,
    NgxFileDropModule
  ],
  exports: []
})
export class ContentModule {
}
