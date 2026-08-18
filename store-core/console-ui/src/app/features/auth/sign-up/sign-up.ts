import {Component, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import {FieldError} from '@shared/ui/form-field/field-error';
import {AuthStory} from '../components/auth-story';
import {AuthFacade} from '../facades/auth.facade';
import {SignUpFormService} from '../services/sign-up-form.service';

@Component({
  selector: 'app-sign-up',
  imports: [AuthStory, FieldError, ReactiveFormsModule, RouterLink, TranslocoDirective],
  templateUrl: './sign-up.html',
  styleUrl: '../auth.css',
})
export class SignUp {
  protected readonly facade = inject(AuthFacade);
  protected readonly form = inject(SignUpFormService).create();
  /** The nested group the template binds to and the server names its field errors against. */
  protected readonly user = this.form.controls.user;

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.facade.createAccount(this.form.getRawValue(), this.form);
  }
}
