import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {StoreAutocompleteComponent} from "./store-autocomplete/store-autocomplete.component";
import {
  NbActionsModule,
  NbAutocompleteModule,
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule, NbDatepickerModule,
  NbDialogModule, NbIconModule,
  NbInputModule,
  NbOptionModule,
  NbRadioModule, NbRouteTabsetModule,
  NbSelectModule,
  NbSpinnerModule, NbTabsetModule,
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
    NbDatepickerModule,
    NbActionsModule,
    NbTabsetModule,
    NbRouteTabsetModule,
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
    NbDatepickerModule.forRoot(),
    NbActionsModule,
    NbTabsetModule,
    NbRouteTabsetModule,
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
