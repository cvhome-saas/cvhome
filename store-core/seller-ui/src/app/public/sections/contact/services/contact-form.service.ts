import {Injectable, inject} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';

export interface ContactMessage {
  name: string;
  email: string;
  subject: string;
  message: string;
}

@Injectable()
export class ContactFormService {
  private readonly fb = inject(FormBuilder);

  readonly form: FormGroup = this.fb.group({
    name: [''],
    email: [''],
    subject: [''],
    message: [''],
  });
}
