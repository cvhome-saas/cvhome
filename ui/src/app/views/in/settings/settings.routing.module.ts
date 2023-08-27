import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {SettingsComponent} from "./views/settings/settings.component";
import {DomainSettingsComponent} from "./views/domain-settings/domain-settings.component";
import {ChannelsSettingsComponent} from "./views/channels-settings/channels-settings.component";

const routes: Routes = [
  {
    path: '', component: SettingsComponent, children: [
      {path: '', component: DomainSettingsComponent},
      {path: 'channels', component: ChannelsSettingsComponent}
    ]
  }
];

// configures NgModule imports and exports
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SettingsRoutingModule {
}
