import {Component, DestroyRef, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import {clearServerErrorsOnChange} from '@cvhome-saas/ui-kit';
import {FormField, TextField} from '@cvhome-saas/ui-kit/ui';
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

  constructor() {
    /*
     * Bound here rather than in the facade, and with *this component's* `DestroyRef`.
     *
     * `AuthFacade` is a root singleton, so subscribing there would keep a subscription to every sign-up form
     * ever rendered alive for the lifetime of the app. The form belongs to the page; so does the subscription.
     *
     * Without it the page has the failure `clearServerErrorsOnChange` exists to prevent, and signup is the one
     * form where it is certain to happen: a duplicate address is the most likely way this call fails, the error
     * lands on the email control, and a server error is not a validator — nothing else will ever remove it. The
     * visitor types a different address, watches the message stay, and cannot submit again.
     */
    clearServerErrorsOnChange(this.form, inject(DestroyRef));

    // The facade is a root singleton and `submitted` latches on success, so a visitor who signs up, lands on
    // sign-in and clicks "Create account" again would otherwise find the button disabled with nothing to say why.
    this.facade.resetSubmission();
  }

  protected submit(): void {
    this.trimTextFields();
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.facade.createAccount(this.form.getRawValue(), this.form);
  }

  /**
   * Removes the whitespace around the name and address, in place, before anything is checked.
   *
   * Two things depend on it. `Validators.required` treats `"   "` as a value, so without this a name of spaces
   * is submitted and stored — the server has no `@NotBlank` to catch it. And the visitor sees the value that is
   * actually posted, rather than one silently cleaned up on the way out.
   *
   * The passwords are not trimmed: a space is a character like any other in a password, and quietly changing
   * one is how an account is created with a secret its owner cannot reproduce.
   */
  private trimTextFields(): void {
    const {firstName, lastName, emailAddress} = this.user.controls;
    for (const control of [firstName, lastName, emailAddress]) {
      const trimmed = control.value.trim();
      if (trimmed !== control.value) {
        control.setValue(trimmed);
      }
    }
  }
}
