import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {SettingsComponent} from "./views/settings/settings.component";
import {DomainSettingsComponent} from "./views/domain-settings/domain-settings.component";
import {StoresSettingsComponent} from "./views/stores-settings/stores-settings.component";

const routes: Routes = [
    {
        path: '', component: SettingsComponent, children: [
            {path: '', component: DomainSettingsComponent},
            {path: 'stores', component: StoresSettingsComponent}
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
