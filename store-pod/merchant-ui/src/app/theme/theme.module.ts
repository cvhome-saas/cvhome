import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {EmptyLayoutComponent} from "./layout/empty/empty.layout";
import {
  NbActionsModule,
  NbButtonModule,
  NbCardModule,
  NbChatModule,
  NbContextMenuModule,
  NbDatepickerModule,
  NbDialogModule,
  NbIconModule,
  NbLayoutModule,
  NbMenuModule,
  NbOptionModule,
  NbSearchModule,
  NbSelectModule,
  NbSidebarModule,
  NbThemeModule,
  NbToastrModule,
  NbUserModule,
  NbWindowModule
} from "@nebular/theme";
import {FooterComponent} from "./components/footer/footer.component";
import {TranslateModule} from '@ngx-translate/core';
import {OneColumnLayoutComponent} from './layout/one-column/one-column.layout';
import {HeaderComponent} from './components/header/header.component';


export const MODULES = [
  NbLayoutModule,
  NbMenuModule,
  NbCardModule,
  NbOptionModule,
  NbSelectModule,



  NbUserModule,
  NbActionsModule,
  NbSearchModule,
  NbSidebarModule,
  NbContextMenuModule,
  NbButtonModule,
  NbIconModule,
  NbSidebarModule,
  NbMenuModule,
  NbDatepickerModule,
  NbDialogModule,
  NbWindowModule,
  NbToastrModule,
  NbChatModule

];
const COMPONENTS = [
  EmptyLayoutComponent,
  OneColumnLayoutComponent,
  HeaderComponent,
  FooterComponent
]

@NgModule({
  declarations: [
    ...COMPONENTS,
  ],
  imports: [
    CommonModule,
    TranslateModule,
    NbThemeModule,
    ...MODULES
  ],
  exports: [
    ...COMPONENTS,
  ],
})
export class ThemeModule {
}
