import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {StoreAutocompleteComponent} from "./store-autocomplete/store-autocomplete.component";
import {
  NbActionsModule,
  NbAutocompleteModule,
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbDatepickerModule,
  NbDialogModule,
  NbIconModule,
  NbInputModule,
  NbOptionModule,
  NbRadioModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbSpinnerModule,
  NbTabsetModule,
  NbToastrModule
} from "@nebular/theme";
import {NgxDatatableModule} from "@swimlane/ngx-datatable";
import {TranslateModule} from "@ngx-translate/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ShowcaseDialogComponent} from "./showcase-dialog/showcase-dialog.component";
import {ImageBrowserComponent} from "./image-browser/image-browser.component";
import {NbEvaIconsModule} from "@nebular/eva-icons";
import {ImageUploadingComponent} from "./image-uploading/image-uploading.component";
import {FilePickerModule} from "ngx-awesome-uploader";
import {QuillModule} from "ngx-quill";


@NgModule({
  declarations: [StoreAutocompleteComponent, ShowcaseDialogComponent, ImageBrowserComponent, ImageUploadingComponent],
  exports: [
    CommonModule,
    StoreAutocompleteComponent,
    ShowcaseDialogComponent,
    ImageBrowserComponent,
    ImageUploadingComponent,
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
    QuillModule,
    TranslateModule,
    FilePickerModule,
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
    QuillModule.forRoot(),
    TranslateModule,
    FilePickerModule,
    FormsModule,
    ReactiveFormsModule,
  ]
})
export class SharedModule {
}
