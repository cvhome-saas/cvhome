import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {UpperCasePipe} from '@angular/common';
import {
  NbButtonGroupModule,
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbIconModule,
  NbInputModule,
  NbSpinnerModule
} from '@nebular/theme';
import {ColumnMode, NgxDatatableModule} from '@swimlane/ngx-datatable';
import {ProductGroupFormFacade} from '../facades/product-group-form.facade';
import {ProductGroupFormService} from '../services/product-group-form.service';
import {ProductAutoCompleteComponent} from '../../../shared/components/product-autocomplete/product-auto-complete.component';

@Component({
  selector: 'ngx-product-group-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TranslateModule,
    UpperCasePipe,
    NbButtonGroupModule,
    NbButtonModule,
    NbCardModule,
    NbCheckboxModule,
    NbIconModule,
    NbInputModule,
    NbSpinnerModule,
    NgxDatatableModule,
    ProductAutoCompleteComponent
  ],
  templateUrl: './product-group-form.component.html',
  styleUrls: ['./product-group-form.component.scss'],
  providers: [ProductGroupFormFacade, ProductGroupFormService]
})
export class ProductGroupFormComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(ProductGroupFormFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }
}
