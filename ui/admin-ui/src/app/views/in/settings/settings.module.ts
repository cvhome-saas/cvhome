import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SettingsComponent} from "./views/settings/settings.component";
import {DomainSettingsComponent} from "./views/domain-settings/domain-settings.component";
import {ChannelsSettingsComponent} from "./views/channels-settings/channels-settings.component";
import {SettingsRoutingModule} from "./settings.routing.module";
import {SubDomainSettingsComponent} from "./componants/sub-domain-settings/sub-domain-settings.component";
import {CustomDomainSettingsComponent} from "./componants/custom-domain-settings/custom-domain-settings.component";
import {HttpClientModule} from "@angular/common/http";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";

@NgModule({
    imports: [
        CommonModule, HttpClientModule, FormsModule, ReactiveFormsModule, SettingsRoutingModule
    ],
    declarations: [SettingsComponent, DomainSettingsComponent, ChannelsSettingsComponent, SubDomainSettingsComponent, CustomDomainSettingsComponent]
})
export class SettingsModule {
}
