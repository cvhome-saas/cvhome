import {Injectable, inject} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';

@Injectable()
export class SignUpFormService {
  private readonly fb = inject(FormBuilder);

  readonly form: FormGroup = this.fb.group({
    user: this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      emailAddress: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
      repeatPassword: ['', Validators.required],
    }, {validators: this.checkPasswords})
  });

  private checkPasswords(group: FormGroup) {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('repeatPassword')?.value;
    return password === confirmPassword ? null : {notSame: true};
  }
}
