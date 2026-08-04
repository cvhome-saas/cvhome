import {Injectable, inject} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {PWD_PATTERN} from '../../constants/user-management.constants';

@Injectable()
export class ChangePasswordFormService {
  private readonly fb = inject(FormBuilder);

  readonly form: FormGroup = this.fb.group({
    password: ['', [Validators.required]],
    newPassword: ['', [Validators.required, Validators.pattern(PWD_PATTERN)]],
    confirmNewPassword: ['', [Validators.required]],
  }, {validator: this.checkPasswords});

  get password() {
    return this.form.get('password');
  }

  get newPassword() {
    return this.form.get('newPassword');
  }

  get confirmNewPassword() {
    return this.form.get('confirmNewPassword');
  }

  private checkPasswords(group: FormGroup) {
    const pass = group.controls.newPassword?.value;
    const confirmPass = group.controls.confirmNewPassword?.value;
    return pass === confirmPass ? null : {notSame: true};
  }
}
