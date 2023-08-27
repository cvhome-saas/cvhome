import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {BaseComponent} from "./views/in/base/base.component";
import {HomeComponent} from "./views/in/home/home.component";
import {ContentComponent} from "./views/in/content/content.component";
import {authenticationGuard} from "./service/auth-guard.service";

const routes: Routes = [
  {
    path: 'in', component: BaseComponent, canActivate: [authenticationGuard()], children: [
      {path: '', component: HomeComponent},
      {path: 'content', component: ContentComponent},
      {path: 'profile', loadChildren: () => import('./views/in/profile/profile.module').then(m => m.ProfileModule)},
      {path: 'settings', loadChildren: () => import('./views/in/settings/settings.module').then(m => m.SettingsModule)}
    ]
  },
  {path: 'welcome', loadChildren: () => import('./views/out/welcome/welcome.module').then(m => m.WelcomeModule)},
  {path: '**', redirectTo: '/in'}
];

// configures NgModule imports and exports
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
