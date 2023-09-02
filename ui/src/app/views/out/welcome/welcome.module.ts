import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HttpClientModule} from "@angular/common/http";
import {WelcomeComponent} from "./views/welcome.component";
import {WelcomeRoutingModule} from "./welcome.routing.module";

@NgModule({
    imports: [
        CommonModule, HttpClientModule, WelcomeRoutingModule
    ],
    declarations: [WelcomeComponent]
})
export class WelcomeModule {
}
