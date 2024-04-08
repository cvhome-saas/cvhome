import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ProductsComponent} from './products.component';
import {Route, RouterModule} from "@angular/router";
import {
  NbButtonModule,
  NbCardModule,
  NbInputModule,
  NbOptionModule,
  NbRadioModule,
  NbSelectModule
} from "@nebular/theme";
import {SharedModule} from "../../shared/shared.module";

const routes: Route[] = [
  {path: "", component: ProductsComponent}
];

@NgModule({
  declarations: [
    ProductsComponent,
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    SharedModule
  ]
})
export class ProductsModule {
}
