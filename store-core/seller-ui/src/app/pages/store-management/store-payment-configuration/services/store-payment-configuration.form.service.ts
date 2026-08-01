import {Injectable, inject} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';

@Injectable()
export class StorePaymentConfigurationFormService {
  private readonly fb = inject(FormBuilder);

  createForm(paymentTypes: string[], configs: any[]): FormGroup {
    const configGroups = paymentTypes.map(type => {
      const config = configs.find(c => c.paymentType === type) || {};
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
