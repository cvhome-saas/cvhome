import {Injectable, inject} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {EMAIL_PATTERN, PWD_PATTERN} from '../../constants/user-management.constants';
import {User} from 'seller-core';

@Injectable()
export class UserFormFormService {
  private readonly fb = inject(FormBuilder);

  form!: FormGroup;

  buildForm(action: string): FormGroup {
    const emailValidators = [Validators.required, Validators.email, Validators.pattern(EMAIL_PATTERN)];
    const disabled = action === 'VIEW';

    this.form = this.fb.group({
      firstName: new FormControl({value: '', disabled}, Validators.required),
      lastName: new FormControl({value: '', disabled}, Validators.required),
      userName: new FormControl({value: '', disabled}, Validators.required),
      emailAddress: new FormControl({value: '', disabled}, emailValidators),
      password: new FormControl({value: '', disabled}),
      repeatPassword: new FormControl({value: '', disabled}),
      active: new FormControl({value: false, disabled}),
      roles: new FormControl({value: [], disabled}),
    }, {validators: this.checkPasswords});

    if (action === 'CREATE') {
      this.form.get('password')?.setValidators([Validators.required, Validators.pattern(PWD_PATTERN)]);
      this.form.get('repeatPassword')?.setValidators([Validators.required]);
    }

    return this.form;
  }

  fillForm(user: User): void {
    if (!this.form || !user) return;
    this.form.get('password')?.clearValidators();
    this.form.patchValue({
      firstName: user.firstName,
      lastName: user.lastName,
      userName: user.userName,
      password: '',
      repeatPassword: '',
      emailAddress: user.emailAddress,
      active: user.active,
      roles: [...(user.roles || [])],
    });
  }

  get userName() {
    return this.form?.get('userName');
  }

  get firstName() {
    return this.form?.get('firstName');
  }

  get lastName() {
    return this.form?.get('lastName');
  }

  get emailAddress() {
    return this.form?.get('emailAddress');
  }

  get password() {
    return this.form?.get('password');
  }

  get repeatPassword() {
    return this.form?.get('repeatPassword');
  }

  get userRoles() {
    return this.form?.get('roles');
  }

  private checkPasswords(group: FormGroup) {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('repeatPassword')?.value;
    return password === confirmPassword ? null : {notSame: true};
  }
}
