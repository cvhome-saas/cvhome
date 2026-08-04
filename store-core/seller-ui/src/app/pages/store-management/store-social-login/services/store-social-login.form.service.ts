import {Injectable, inject} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {ReadableSocialLoginConfig} from '../../models/store-service.model';

@Injectable()
export class StoreSocialLoginFormService {
  private readonly fb = inject(FormBuilder);

  createForm(providers: string[], configs: ReadableSocialLoginConfig[]): FormGroup {
    const configGroups = providers.map(provider => {
      const config: ReadableSocialLoginConfig = configs.find(c => c.providerId === provider) || {};
      return this.fb.group({
        providerId: [provider],
        appId: [config.appId || ''],
        appSecret: [config.appSecret || ''],
        enabled: [config.enabled !== undefined ? config.enabled : false]
      });
    });
    return this.fb.group({
      configs: this.fb.array(configGroups)
    });
  }
}
