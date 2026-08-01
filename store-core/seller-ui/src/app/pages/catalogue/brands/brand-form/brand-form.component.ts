import {Component, Input, OnInit, inject, DestroyRef} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {
  NbButtonModule,
  NbCardModule,
  NbInputModule,
  NbOptionModule,
  NbSelectModule,
  NbSpinnerModule
} from '@nebular/theme';
import {QuillModule} from 'ngx-quill';
import {ReadableMerchantStore} from '../../../store-management/models/store';
import {BrandFormFacade} from '../facades/brand-form.facade';
import {BrandFormService} from '../services/brand-form.service';
import {ReadableManufacturer} from '../models/brand.model';

@Component({
  selector: 'ngx-brand-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TranslateModule,
    NbButtonModule,
    NbCardModule,
    NbInputModule,
    NbOptionModule,
    NbSelectModule,
    NbSpinnerModule,
    QuillModule
  ],
  templateUrl: './brand-form.component.html',
  styleUrls: ['./brand-form.component.scss'],
  providers: [BrandFormFacade, BrandFormService]
})
export class BrandFormComponent implements OnInit {
  @Input() brand: ReadableManufacturer;
  @Input() title: string;
  @Input() store: ReadableMerchantStore;

  protected readonly facade = inject(BrandFormFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.brand, this.store, this.destroyRef);
  }
}
