import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppComponent} from './app.component';
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {LeftSideBarNavComponent} from './componants/left-side-bar-nav/left-side-bar-nav.component';
import {BaseComponent} from './views/in/base/base.component';
import {HeaderNavComponent} from './componants/header-nav/header-nav.component';
import {ContentComponent} from './views/in/content/content.component';
import {WelcomeComponent} from './views/out/welcome/welcome.component';
import {AppRoutingModule} from "./app-routing.module";
import {HomeComponent} from './views/in/home/home.component';
import {SettingsComponent} from './views/in/settings/settings.component';
import {ProfileComponent} from './views/in/profile/profile.component';
import {ErrorComponent} from './views/out/error/error.component';
import {
  SubDomainSettingsComponent
} from './views/in/settings/componants/sub-domain-settings/sub-domain-settings.component';
import {
  CustomDomainSettingsComponent
} from './views/in/settings/componants/custom-domain-settings/custom-domain-settings.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MapErrorsPipe} from './pipe/map-errors.pipe';
import {CsrfInterceptor} from "./interceptors/csrf-interceptor";

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
    ErrorComponent,
    SubDomainSettingsComponent,
    CustomDomainSettingsComponent,
    MapErrorsPipe
  ],
  imports: [
    BrowserModule, HttpClientModule, AppRoutingModule, FormsModule, ReactiveFormsModule
  ],
  providers: [
    // {provide: HTTP_INTERCEPTORS, useClass: CsrfInterceptor, multi: true}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
