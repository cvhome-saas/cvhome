import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HttpClientModule} from "@angular/common/http";
import {ProfileComponent} from "./views/profile/profile.component";
import {ProfileRoutingModule} from "./profile.routing.module";

@NgModule({
    imports: [
        CommonModule, HttpClientModule, ProfileRoutingModule
    ],
    declarations: [ProfileComponent]
})
export class ProfileModule {
}
