import {Component, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import {FormField} from '@shared/ui/form-field/form-field';
import {TextField} from '@shared/ui/text-field/text-field';
import {AuthStory} from '../components/auth-story';
import {AuthFacade} from '../facades/auth.facade';
import {SignUpFormService} from '../services/sign-up-form.service';

@Component({
  selector: 'app-sign-up',
  imports: [AuthStory, FormField, ReactiveFormsModule, RouterLink, TextField, TranslocoDirective],
  templateUrl: './sign-up.html',
  styleUrls: ['../../../shared/styles/field.css', '../auth.css'],
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
