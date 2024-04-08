import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CategoryComponent} from './category.component';
import {RouterModule, Routes} from "@angular/router";
import {
  NbButtonModule,
  NbCardModule,
  NbInputModule,
  NbOptionModule,
  NbRadioModule,
  NbSelectModule
} from "@nebular/theme";

const routes: Routes = [
  {path: '', component: CategoryComponent},
];


@NgModule({
  declarations: [
    CategoryComponent
  ],
  imports: [
    CommonModule, RouterModule.forChild(routes),
    NbCardModule, NbButtonModule, NbInputModule, NbRadioModule, NbOptionModule, NbSelectModule
  ]
})
export class CategoryModule {
}
