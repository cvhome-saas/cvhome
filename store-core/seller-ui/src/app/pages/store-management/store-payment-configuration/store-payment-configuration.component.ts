import {Component, OnInit, inject} from '@angular/core';
import {StorePaymentConfigurationFacade} from './facades/store-payment-configuration.facade';
import {StorePaymentConfigurationFormService} from './services/store-payment-configuration.form.service';

@Component({
  selector: 'ngx-store-payment-configuration',
  standalone: false,
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
