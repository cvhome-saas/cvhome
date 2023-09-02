import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {InRoutingModule} from "./in.routing.module";
import {BaseComponent} from "./base/base.component";
import {ComponentsSharedModule} from "../../shared/components/components-shared.module";
import {MapErrorsPipe} from "../../shared/utils/pipe/map-errors.pipe";
import {PipesSharedModule} from "../../shared/utils/pipes-shared.module";

@NgModule({
  imports: [
    CommonModule, InRoutingModule, ComponentsSharedModule, PipesSharedModule
  ],
  declarations: [
    BaseComponent,

  ]
})
export class InModule {
}
