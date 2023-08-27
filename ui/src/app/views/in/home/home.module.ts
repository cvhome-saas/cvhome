import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HttpClientModule} from "@angular/common/http";
import {HomeRoutingModule} from "./home.routing.module";
import {HomeComponent} from "./views/home.component";

@NgModule({
  imports: [
    CommonModule, HttpClientModule, HomeRoutingModule
  ],
  declarations: [HomeComponent]
})
export class HomeModule {
}
