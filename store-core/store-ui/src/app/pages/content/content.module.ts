import { NgModule } from '@angular/core';
import { ContentRoutingModule, routedComponents } from './content-routing.module';
import { NbDialogModule } from '@nebular/theme';
import { LightboxModule } from 'ngx-lightbox';
import { NgxSummernoteModule } from 'ngx-summernote';
import {SharedModule} from "../store-manager/shared/shared.module";
import {DropzoneCdkModule} from "@ngx-dropzone/cdk";
@NgModule({
  declarations: [
    ...routedComponents
  ],
  imports: [
    ContentRoutingModule,
    SharedModule,
    NbDialogModule.forChild(),
    NgxSummernoteModule,
    DropzoneCdkModule,
    LightboxModule,
  ],
  exports: []
})
export class ContentModule { }
