import {Injectable, inject} from '@angular/core';
import {AbstractControl, NonNullableFormBuilder, ValidationErrors, Validators} from '@angular/forms';

/**
 * The shortest password the console will submit.
 *
 * This is not a mirror of a server rule — it is the only rule. See `SignUpFormService` below.
 */
export const MIN_PASSWORD_LENGTH = 8;

/**
 * The public signup form.
 *
 * The group is nested under `user` to mirror tenancy's `CreateOrgRequest(PersistableUser user)` exactly, so
 * `getRawValue()` is posted unmapped and any server `fieldErrors[]` path (`user.emailAddress`) resolves to the
 * control that caused it.
 *
 * **The validation here is the only validation there is.** `POST /tenancy/api/v1/signup/public/create` was
 * probed against the running stack and accepted empty names, `not-an-email` as an address, a one-character
 * password and a mismatched confirmation — 200, account created. There is no `@Valid` on `CreateOrgRequest`
 * and no password policy behind it. An earlier revision of this file deferred the password rules to the
 * server on the assumption that uaa owned them; it does not. See lessons.md, "Auth — public signup
 * validates nothing".
 */
@Injectable({providedIn: 'root'})
export class SignUpFormService {
  private readonly fb = inject(NonNullableFormBuilder);

  create() {
    return this.fb.group({
      user: this.fb.group(
        {
          firstName: ['', Validators.required],
          lastName: ['', Validators.required],
          emailAddress: ['', [Validators.required, Validators.email]],
          password: ['', [Validators.required, Validators.minLength(MIN_PASSWORD_LENGTH)]],
          repeatPassword: ['', Validators.required],
        },
        {validators: passwordsMatch},
      ),
    });
  }
}

/**
 * Checked here as well as by uaa, which also compares the two. The round trip is the point of doing it locally —
 * a mistyped confirmation is the one signup failure worth catching without asking the server.
 */
export function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const repeat = group.get('repeatPassword')?.value;
  return password === repeat ? null : {passwordMismatch: true};
}
