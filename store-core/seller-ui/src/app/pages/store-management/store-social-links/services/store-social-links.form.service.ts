import {Injectable, inject} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';

@Injectable()
export class StoreSocialLinksFormService {
  private readonly fb = inject(FormBuilder);

  createForm(store: any, providers: string[]): FormGroup {
    const links: Record<string, string> = {};
    const controls: Record<string, any> = {};

    if (store && store.socialLinks) {
      store.socialLinks.forEach((it: any) => {
        links[it.provider] = it.url;
      });
    }

    providers.forEach(it => {
      controls[it] = [links[it] || '', []];
    });

    return this.fb.group(controls);
  }
}
