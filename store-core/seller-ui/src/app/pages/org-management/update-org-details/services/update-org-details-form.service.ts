import {Injectable, inject} from '@angular/core';
import {FormBuilder, FormControl, FormGroup} from '@angular/forms';
import {Org} from '../../model/org';

@Injectable()
export class UpdateOrgDetailsFormService {
  private readonly fb = inject(FormBuilder);

  readonly form: FormGroup = this.fb.group({
    email: new FormControl({value: '', disabled: true}),
    subscriptionPlan: new FormControl(''),
  });

  fillForm(org: Org): void {
    if (!org) return;
    this.form.patchValue({
      email: org.email?.email || '',
      subscriptionPlan: org.subscriptionPlan || '',
    });
  }
}
