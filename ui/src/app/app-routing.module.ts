import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {BaseComponent} from "./views/in/base/base.component";
import {WelcomeComponent} from "./views/out/welcome/welcome.component";
import {HomeComponent} from "./views/in/home/home.component";
import {ContentComponent} from "./views/in/content/content.component";
import {authenticationGuard} from "./service/auth-guard.service";
import {ProfileComponent} from "./views/in/profile/profile.component";

const routes: Routes = [
  {
    path: 'in', component: BaseComponent, canActivate: [authenticationGuard()], children: [
      {path: '', component: HomeComponent},
      {path: 'content', component: ContentComponent},
      {path: 'profile', component: ProfileComponent},
      {path: 'settings', loadChildren: () => import('./views/in/settings/settings.module').then(m => m.SettingsModule)}
    ]
  },
  {path: 'welcome', component: WelcomeComponent},
  {path: '**', redirectTo: '/in'}
];

// configures NgModule imports and exports
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
