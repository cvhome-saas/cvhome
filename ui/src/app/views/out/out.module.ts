import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {OutRoutingModule} from "./out.routing.module";
import {ErrorComponent} from "./error/error.component";

@NgModule({
  imports: [
    CommonModule, OutRoutingModule
  ],
  declarations: [ErrorComponent]
})
export class OutModule {
}
