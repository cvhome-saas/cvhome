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

const routes: Route[] = [
  {path: "", component: ProductsComponent}
];

@NgModule({
  declarations: [
    ProductsComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    NbCardModule, NbButtonModule, NbInputModule, NbRadioModule, NbOptionModule, NbSelectModule

  ]
})
export class ProductsModule {
}
