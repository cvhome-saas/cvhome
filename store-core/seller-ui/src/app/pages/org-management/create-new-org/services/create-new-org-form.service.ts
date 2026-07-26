import {Injectable, inject} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {EMAIL_PATTERN, PWD_PATTERN} from '../../constants/org-management.constants';

@Injectable()
export class CreateNewOrgFormService {
  private readonly fb = inject(FormBuilder);

  readonly form: FormGroup = this.fb.group({
    user: this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      emailAddress: ['', [Validators.required, Validators.email, Validators.pattern(EMAIL_PATTERN)]],
      password: ['', [Validators.required, Validators.pattern(PWD_PATTERN)]],
      repeatPassword: ['', [Validators.required]]
    }),
    subscriptionPlan: ['', [Validators.required]]
  }, {validators: this.checkPasswords});

  get userGroup(): FormGroup {
    return this.form.get('user') as FormGroup;
  }

  setSubscriptionPlans(plans: string[]): void {
    if (plans && plans.length > 0) {
      this.form.patchValue({
        subscriptionPlan: plans[0]
      });
    }
  }

  private checkPasswords(group: FormGroup) {
    const password = group.get('user.password')?.value;
    const confirmPassword = group.get('user.repeatPassword')?.value;
    return password === confirmPassword ? null : {notSame: true};
  }
}
