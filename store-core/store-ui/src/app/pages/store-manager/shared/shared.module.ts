import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {StoreAutocompleteComponent} from "./store-autocomplete/store-autocomplete.component";
import {
  NbAutocompleteModule,
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbDialogModule, NbIconModule,
  NbInputModule,
  NbOptionModule,
  NbRadioModule,
  NbSelectModule,
  NbSpinnerModule,
  NbToastrModule
} from "@nebular/theme";
import {NgxDatatableModule} from "@swimlane/ngx-datatable";
import {TranslateModule} from "@ngx-translate/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ShowcaseDialogComponent} from "./showcase-dialog/showcase-dialog.component";
import {NgxSummernoteModule} from "ngx-summernote";
import {ImageBrowserComponent} from "./image-browser/image-browser.component";
import {NbEvaIconsModule} from "@nebular/eva-icons";


@NgModule({
  declarations: [StoreAutocompleteComponent, ShowcaseDialogComponent, ImageBrowserComponent],
  exports: [
    CommonModule,
    StoreAutocompleteComponent,
    ShowcaseDialogComponent,
    ImageBrowserComponent,
    NbSelectModule,
    NbCardModule,
    NbButtonModule,
    NbInputModule,
    NbRadioModule,
    NbOptionModule,
    NbSelectModule,
    NbSpinnerModule,
    NbCheckboxModule,
    NbToastrModule,
    NbDialogModule,
    NbAutocompleteModule,
    NbEvaIconsModule,
    NbIconModule,
    NgxDatatableModule,
    NgxSummernoteModule,
    TranslateModule,
    FormsModule,
    ReactiveFormsModule,
  ],
  imports: [
    CommonModule,
    NbSelectModule,
    NbCardModule,
    NbButtonModule,
    NbInputModule,
    NbRadioModule,
    NbOptionModule,
    NbSelectModule,
    NbSpinnerModule,
    NbCheckboxModule,
    NbToastrModule.forRoot({}),
    NbDialogModule.forRoot({}),
    NbAutocompleteModule,
    NbEvaIconsModule,
    NbIconModule,
    NgxDatatableModule,
    NgxSummernoteModule,
    TranslateModule,
    FormsModule,
    ReactiveFormsModule,
  ]
})
export class SharedModule {
}
