import {Injectable, inject} from '@angular/core';
import {NonNullableFormBuilder, Validators} from '@angular/forms';

import type {ContactTopicId} from '@models/marketing';

@Injectable({providedIn: 'root'})
export class ContactFormService {
  private readonly fb = inject(NonNullableFormBuilder);

  create() {
    return this.fb.group({
      name: ['', Validators.required],
      organization: [''],
      email: ['', [Validators.required, Validators.email]],
      topic: this.fb.control<ContactTopicId>('migratingStores', Validators.required),
      message: ['', [Validators.required, Validators.minLength(10)]],
    });
  }
}
