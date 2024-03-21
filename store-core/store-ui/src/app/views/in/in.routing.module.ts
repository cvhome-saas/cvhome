import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {BaseComponent} from "./base/base.component";


const routes: Routes = [
    {
        path: '', component: BaseComponent,
        children: [
            {path: '', loadChildren: () => import('./home/home.module').then(m => m.HomeModule)},
            {
                path: 'profile',
                loadChildren: () => import('./profile/profile.module').then(m => m.ProfileModule)
            },
            {
                path: 'store-manager',
                loadChildren: () => import('./store-manager/store-manager.module').then(m => m.StoreManagerModule)
            },
            {
                path: 'settings',
                loadChildren: () => import('./settings/settings.module').then(m => m.SettingsModule)
            }
        ]
    }
];

// configures NgModule imports and exports
@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class InRoutingModule {
}
