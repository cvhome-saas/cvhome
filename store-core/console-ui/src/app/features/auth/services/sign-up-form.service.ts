import {Injectable, inject} from '@angular/core';
import {NonNullableFormBuilder, Validators} from '@angular/forms';

@Injectable({providedIn: 'root'})
export class SignUpFormService {
  private readonly fb = inject(NonNullableFormBuilder);

  create() {
    return this.fb.group({
      name: ['', Validators.required],
      organization: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
    });
  }
}
