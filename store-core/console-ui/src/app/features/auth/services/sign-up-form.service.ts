import {Injectable, inject} from '@angular/core';
import {AbstractControl, NonNullableFormBuilder, ValidationErrors, Validators} from '@angular/forms';

/**
 * The public signup form.
 *
 * The group is nested under `user` to mirror tenancy's `CreateOrgRequest(PersistableUser user)` exactly, which is
 * what lets `getRawValue()` be posted unmapped and — the reason that matters — lets `ApiErrorService.applyToForm`
 * land a server `fieldErrors[]` path like `user.emailAddress` on the control that caused it.
 *
 * No client-side password policy beyond "required". uaa owns the rules and answers with a field error naming the
 * one that failed; a guessed minimum here would reject passwords the server accepts, or accept ones it does not.
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
          password: ['', Validators.required],
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
