import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {StoreSelectorComponent} from "./store-selector/store-selector.component";
import {
  NbButtonModule,
  NbCardModule,
  NbInputModule,
  NbOptionModule,
  NbRadioModule,
  NbSelectModule, NbSpinnerModule
} from "@nebular/theme";
import {NgxDatatableModule} from "@swimlane/ngx-datatable";
import {TranslateModule} from "@ngx-translate/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";


@NgModule({
  declarations: [StoreSelectorComponent],
  exports: [
    StoreSelectorComponent,
    NbSelectModule,
    NbCardModule,
    NbButtonModule,
    NbInputModule,
    NbRadioModule,
    NbOptionModule,
    NbSelectModule,
    NgxDatatableModule,
    NbSpinnerModule,
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
    NgxDatatableModule,
    NbSpinnerModule,
    TranslateModule,
    FormsModule,
    ReactiveFormsModule,
  ]
})
export class SharedModule {
}
