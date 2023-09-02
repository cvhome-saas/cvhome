import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {BaseComponent} from "./base/base.component";
import {authenticationGuard} from "../../service/auth-guard.service";


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
