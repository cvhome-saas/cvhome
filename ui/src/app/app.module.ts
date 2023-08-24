import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppComponent} from './app.component';
import {HttpClientModule} from "@angular/common/http";
import {LeftSideBarNavComponent} from './componants/left-side-bar-nav/left-side-bar-nav.component';
import {BaseComponent} from './views/in/base/base.component';
import {HeaderNavComponent} from './componants/header-nav/header-nav.component';
import {ContentComponent} from './views/in/content/content.component';
import {WelcomeComponent} from './views/out/welcome/welcome.component';
import {AppRoutingModule} from "./app-routing.module";
import { HomeComponent } from './views/in/home/home.component';
import { SettingsComponent } from './views/in/settings/settings.component';
import { ProfileComponent } from './views/in/profile/profile.component';
import { ErrorComponent } from './views/out/error/error.component';

@NgModule({
  declarations: [
    AppComponent,
    LeftSideBarNavComponent,
    BaseComponent,
    HeaderNavComponent,
    ContentComponent,
    WelcomeComponent,
    HomeComponent,
    SettingsComponent,
    ProfileComponent,
    ErrorComponent
  ],
  imports: [
    BrowserModule, HttpClientModule, AppRoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
