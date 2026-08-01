import {Component, OnInit, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbInputModule,
  NbOptionModule,
  NbSelectModule
} from '@nebular/theme';
import {StorePaymentConfigurationFacade} from './facades/store-payment-configuration.facade';
import {StorePaymentConfigurationFormService} from './services/store-payment-configuration.form.service';

@Component({
  selector: 'ngx-store-payment-configuration',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TranslateModule,
    NbButtonModule,
    NbCardModule,
    NbCheckboxModule,
    NbInputModule,
    NbOptionModule,
    NbSelectModule
  ],
  templateUrl: './store-payment-configuration.component.html',
  styleUrls: ['./store-payment-configuration.component.scss'],
  providers: [StorePaymentConfigurationFacade, StorePaymentConfigurationFormService]
})
export class StorePaymentConfigurationComponent implements OnInit {
  protected readonly facade = inject(StorePaymentConfigurationFacade);

  ngOnInit() {
    this.facade.init();
  }
}
