import {Component, DestroyRef, Input, OnInit, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {
  NbActionsModule,
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbDatepickerModule,
  NbInputModule,
  NbOptionModule,
  NbRouteTabsetModule,
  NbSelectModule,
  NbSpinnerModule
} from '@nebular/theme';
import {QuillModule} from 'ngx-quill';
import {ProductFormFacade} from '../facades/product-form.facade';
import {ProductFormService} from '../services/product-form.service';

@Component({
  selector: 'ngx-product-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TranslateModule,
    NbActionsModule,
    NbButtonModule,
    NbCardModule,
    NbCheckboxModule,
    NbDatepickerModule,
    NbInputModule,
    NbOptionModule,
    NbRouteTabsetModule,
    NbSelectModule,
    NbSpinnerModule,
    QuillModule
  ],
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.scss'],
  providers: [ProductFormFacade, ProductFormService]
})
export class ProductFormComponent implements OnInit {
  @Input() product: any;
  @Input() _title: string;

  protected readonly facade = inject(ProductFormFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.product, this.destroyRef);
  }
}
