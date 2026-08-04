import {Routes} from '@angular/router';
import {CategoryCreationComponent} from './category-creation/category-creation.component';
import {CategoriesComponent} from './categories.component';
import {CategoriesListComponent} from './categories-list/categories-list.component';
import {CategoriesHierarchyComponent} from './categories-hierarchy/categories-hierarchy.component';
import {CategoryDetailComponent} from './category-detail/category-detail.component';

export const CATEGORIES_ROUTES: Routes = [
  {
    path: '',
    component: CategoriesComponent,
    children: [
      {
        path: 'create-category',
        component: CategoryCreationComponent,
      },
      {
        path: 'categories-list',
        component: CategoriesListComponent,
      },
      {
        path: 'categories-hierarchy',
        component: CategoriesHierarchyComponent,
      },
      {
        path: 'category/:id',
        component: CategoryDetailComponent,
      },
    ],
  }
];
