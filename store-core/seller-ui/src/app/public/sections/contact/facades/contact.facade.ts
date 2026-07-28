import {Injectable, inject} from '@angular/core';
import {ContactFormService} from '../services/contact-form.service';

@Injectable()
export class ContactFacade {
  readonly formService = inject(ContactFormService);

  submit(): void {
    if (this.formService.form.invalid) return;
    this.formService.form.reset({});
  }
}
