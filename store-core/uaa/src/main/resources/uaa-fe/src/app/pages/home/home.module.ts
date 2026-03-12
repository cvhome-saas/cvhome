import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';
import {ReactiveFormsModule} from '@angular/forms';
import {HomeRoutingModule} from './home-routing.module';
import {ThemeModule} from './theme/theme.module';
import {SharedModule} from './shared/shared.module';


@NgModule({
  declarations: [
  ],
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    HomeRoutingModule,
    ThemeModule,
    SharedModule,
  ]
})
export class HomeModule {
}
