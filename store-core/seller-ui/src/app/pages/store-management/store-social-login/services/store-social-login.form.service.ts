import {Injectable, inject} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';

@Injectable()
export class StoreSocialLoginFormService {
  private readonly fb = inject(FormBuilder);

  createForm(providers: string[], configs: any[]): FormGroup {
    const configGroups = providers.map(provider => {
      const config = configs.find(c => c.id?.providerId === provider) || {};
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
