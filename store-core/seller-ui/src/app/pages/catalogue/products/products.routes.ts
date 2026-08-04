import {Routes} from '@angular/router';
import {ProductsComponent} from './products.component';
import {ProductsListComponent} from './products-list/products-list.component';
import {ProductCreationComponent} from './product-creation/product-creation.component';
import {ProductDetailsComponent} from './product-details/product-details.component';
import {ProductsImagesComponent} from './products-images/products-images.component';
import {ProductToCategoryComponent} from './product-to-category/product-to-category.component';
import {ProductRelatedComponent} from './product-related/product-related.component';

export const PRODUCTS_ROUTES: Routes = [
  {
    path: '',
    component: ProductsComponent,
    children: [
      {
        path: 'create-product',
        component: ProductCreationComponent,
      },
      {
        path: 'products-list',
        component: ProductsListComponent,
      },
      {
        path: 'product/:code',
        component: ProductDetailsComponent,

        children: [
          {
            path: '',
            redirectTo: 'default',
            pathMatch: 'full',
          },
          {
            path: 'default',
            component: ProductsImagesComponent,
          },
          {
            path: 'images',
            component: ProductsImagesComponent,
          },
          {
            path: 'category',
            component: ProductToCategoryComponent,
          },
          {
            path: 'related',
            component: ProductRelatedComponent,
          },
        ],
      },
    ],
  }
];
