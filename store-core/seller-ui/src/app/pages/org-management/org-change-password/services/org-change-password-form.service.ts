import {Injectable, inject} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {PWD_PATTERN} from '../../constants/org-management.constants';

@Injectable()
export class OrgChangePasswordFormService {
  private readonly fb = inject(FormBuilder);

  readonly form: FormGroup = this.fb.group({
    newPassword: ['', [Validators.required, Validators.pattern(PWD_PATTERN)]],
    confirmNewPassword: ['', [Validators.required]],
  }, {validators: this.checkPasswords});

  private checkPasswords(group: FormGroup) {
    const pass = group.controls.newPassword?.value;
    const confirmPass = group.controls.confirmNewPassword?.value;
    return pass === confirmPass ? null : {notSame: true};
  }
}
