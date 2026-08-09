import {Injectable, inject} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {ReadableMerchantStoreWithPod} from 'seller-core/stores';

@Injectable()
export class StoreSocialLinksFormService {
  private readonly fb = inject(FormBuilder);

  createForm(store: ReadableMerchantStoreWithPod, providers: string[]): FormGroup {
    const links: Record<string, string> = {};
    const controls: Record<string, unknown[]> = {};

    if (store && store.socialLinks) {
      store.socialLinks.forEach((it) => {
        links[it.provider] = it.url;
      });
    }

    providers.forEach(it => {
      controls[it] = [links[it] || '', []];
    });

    return this.fb.group(controls);
  }
}
