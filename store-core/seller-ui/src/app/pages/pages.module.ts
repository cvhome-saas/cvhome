import {NgModule} from '@angular/core';

import {PagesComponent} from './pages.component';
import {PagesRoutingModule} from './pages-routing.module';
import {ThemeModule} from '../theme/theme.module';
import {NotFoundComponent} from './not-found/not-found.component';
import {SharedModule} from '../shared/shared.module';

@NgModule({
  exports: [],
  imports: [
    PagesRoutingModule,
    ThemeModule,
    SharedModule,
  ],
  declarations: [
    PagesComponent,
    NotFoundComponent
  ],
})
export class PagesModule {
}
