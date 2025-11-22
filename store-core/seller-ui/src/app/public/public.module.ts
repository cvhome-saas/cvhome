import {NgModule} from '@angular/core';
import {PublicRoutingModule} from './public-routing.module';
import {ThemeModule} from '../pages/theme/theme.module';
import {SharedModule} from "../pages/shared/shared.module";
import {PublicComponent} from "./public.component";
import {HeaderComponent} from "./components/header/header.component";
import {FooterComponent} from "./components/footer/footer.component";


@NgModule({
    declarations: [PublicComponent],
  imports: [
    PublicRoutingModule,
    ThemeModule,
    SharedModule,
    HeaderComponent,
    FooterComponent,
  ]
})
export class PublicModule {
}
