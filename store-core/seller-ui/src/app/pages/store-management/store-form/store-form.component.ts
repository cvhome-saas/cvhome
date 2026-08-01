import {Component, Input, OnChanges, OnInit, SimpleChanges, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbDatepickerModule,
  NbInputModule,
  NbOptionModule,
  NbSelectModule,
  NbSpinnerModule
} from '@nebular/theme';
import {StoreFormFacade} from './facades/store-form.facade';
import {StoreFormService} from './services/store-form.service';
import {ReadableMerchantStoreWithPod} from '../models/store-service.model';

@Component({
  selector: 'ngx-store-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TranslateModule,
    NbButtonModule,
    NbCardModule,
    NbCheckboxModule,
    NbDatepickerModule,
    NbInputModule,
    NbOptionModule,
    NbSelectModule,
    NbSpinnerModule
  ],
  templateUrl: './store-form.component.html',
  styleUrls: ['./store-form.component.scss'],
  providers: [StoreFormFacade, StoreFormService]
})
export class StoreFormComponent implements OnInit, OnChanges {
  @Input() title: string;
  @Input() store: ReadableMerchantStoreWithPod;
  @Input() isCancel: string;

  protected readonly facade = inject(StoreFormFacade);

  ngOnInit() {
    this.facade.init(this.store);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['store'] && !changes['store'].isFirstChange()) {
      this.facade.setStore(this.store);
    }
  }
}
