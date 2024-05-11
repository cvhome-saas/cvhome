import {NgModule} from '@angular/core';
import {NbCardModule, NbMenuModule, NbOptionModule, NbSelectModule} from '@nebular/theme';

import {ThemeModule} from '../@theme/theme.module';
import {PagesComponent} from './pages.component';
import {PagesRoutingModule} from './pages-routing.module';

@NgModule({
  exports: [],
  imports: [
    PagesRoutingModule,
    ThemeModule,
    NbMenuModule,
    NbCardModule,
    NbOptionModule,
    NbSelectModule,
  ],
  declarations: [
    PagesComponent
  ],
})
export class PagesModule {
}
