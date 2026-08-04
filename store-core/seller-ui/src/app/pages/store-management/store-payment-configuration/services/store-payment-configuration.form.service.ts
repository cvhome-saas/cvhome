import {Injectable, inject} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {ReadablePaymentConfiguration} from '../../models/store-service.model';

@Injectable()
export class StorePaymentConfigurationFormService {
  private readonly fb = inject(FormBuilder);

  createForm(paymentTypes: string[], configs: ReadablePaymentConfiguration[]): FormGroup {
    const configGroups = paymentTypes.map(type => {
      const config: ReadablePaymentConfiguration = configs.find(c => c.paymentType === type) || {};
      return this.fb.group({
        exists: [configs.some(c => c.paymentType === type)],
        paymentType: [type],
        apiKey: [config.apiKey || ''],
        secretKey: [config.secretKey || ''],
        webhookSecret: [config.webhookSecret || ''],
        enabled: [config.enabled !== undefined ? config.enabled : false]
      });
    });
    return this.fb.group({
      configs: this.fb.array(configGroups)
    });
  }
}
