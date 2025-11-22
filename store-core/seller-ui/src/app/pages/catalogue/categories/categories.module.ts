import {NgModule} from '@angular/core';

import {CategoriesComponent} from './categories.component';
import {CategoriesRoutingModule} from './categories-routing.module';
import {CategoryCreationComponent} from './category-creation/category-creation.component';
import {CategoriesListComponent} from './categories-list/categories-list.component';
import {CategoriesHierarchyComponent} from './categories-hierarchy/categories-hierarchy.component';
import {CategoryFormComponent} from './category-form/category-form.component';
import {CategoryDetailComponent} from './category-detail/category-detail.component';
import {NbDialogModule} from '@nebular/theme';
import {SharedModule} from "../../shared/shared.module";
import {CategoriesVisibilityComponent} from "./categories-list/categories-visibility.component";


@NgModule({
  declarations: [
    CategoriesComponent,
    CategoryCreationComponent,
    CategoriesListComponent,
    CategoriesHierarchyComponent,
    CategoriesVisibilityComponent,
    CategoryFormComponent,
    CategoryDetailComponent,
  ],
  imports: [
    CategoriesRoutingModule,
    SharedModule,
    NbDialogModule.forChild(),
  ],
  providers: []
})

export class CategoriesModule {
}
