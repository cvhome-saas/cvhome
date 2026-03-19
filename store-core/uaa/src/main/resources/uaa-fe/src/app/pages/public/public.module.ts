import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';
import {ReactiveFormsModule} from '@angular/forms';
import {PublicRoutingModule} from './public-routing.module';


@NgModule({
  declarations: [
  ],
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    PublicRoutingModule
  ]
})
export class PublicModule {
}
