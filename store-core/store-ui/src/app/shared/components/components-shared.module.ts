import {NgModule} from "@angular/core";
import {LeftSideBarNavComponent} from "./left-side-bar-nav/left-side-bar-nav.component";
import {HeaderNavComponent} from "./header-nav/header-nav.component";
import {CommonModule} from "@angular/common";
import {RouterLink} from "@angular/router";

@NgModule({
  declarations: [
    LeftSideBarNavComponent,
    HeaderNavComponent,
  ],
    imports: [
        CommonModule,
        RouterLink
    ],
  exports: [
    CommonModule,
    LeftSideBarNavComponent,
    HeaderNavComponent
  ]
})
export class ComponentsSharedModule {
}
