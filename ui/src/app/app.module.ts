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
import {HomeComponent} from './views/in/home/home.component';
import {ErrorComponent} from './views/out/error/error.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MapErrorsPipe} from './pipe/map-errors.pipe';

@NgModule({
  declarations: [
    AppComponent,
    LeftSideBarNavComponent,
    BaseComponent,
    HeaderNavComponent,
    ContentComponent,
    WelcomeComponent,
    HomeComponent,
    ErrorComponent,
    MapErrorsPipe,
  ],
  imports: [
    BrowserModule, HttpClientModule, AppRoutingModule, FormsModule, ReactiveFormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
