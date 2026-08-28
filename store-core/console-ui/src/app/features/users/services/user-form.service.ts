import {Injectable, inject} from '@angular/core';
import {FormControl, FormGroup, NonNullableFormBuilder, Validators} from '@angular/forms';

import {passwordsMatch} from '@shared/validators/passwords-match';
import type {TeamRow} from '@models/team';

/**
 * The password rule, and what it is a rule about.
 *
 * **There is no server-side password policy at all.** `AdminService.resetPassword` encodes whatever
 * string it is handed, and `CreateUserRequest` validates only that the username is non-blank and the
 * email looks like one. So this is not a mirror of a server rule — it *is* the rule, and it is
 * enforced in exactly one place. See lessons.md, "Users — no password policy anywhere".
 *
 * Upper, lower and a digit, carried over from seller-ui's `PWD_PATTERN`. **Its 12-character upper
 * bound is not carried over**: a maximum length on a password blocks passphrases, which are stronger
 * than anything this rule can otherwise ask for, and bcrypt has no trouble with them.
 */
export const PASSWORD_PATTERN = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d).{8,}$/;

/**
 * A username, as uaa will store it.
 *
 * `uaa.users.username` is `varchar(190) NOT NULL UNIQUE` and constrained by nothing else, so this is
 * the console's own rule: no spaces, because it is typed into a sign-in box.
 */
export const USERNAME_PATTERN = /^[A-Za-z0-9._-]+$/;

export type UserForm = FormGroup<{
  userName: FormControl<string>;
  firstName: FormControl<string>;
  lastName: FormControl<string>;
  emailAddress: FormControl<string>;
  active: FormControl<boolean>;
  roles: FormControl<readonly string[]>;
  password: FormControl<string>;
  repeatPassword: FormControl<string>;
}>;

/**
 * The team page's create/edit form.
 *
 * Built here rather than in the component or the facade, per `ARCHITECTURE.md` §5.
 *
 * **There is no uniqueness check.** Every other create form in this console pre-flights its key with
 * `uniqueAsync` — a store name, a product SKU, a category code — because each has a
 * `…/unique?code=` endpoint behind it. A username has none reachable from here: uaa exposes
 * `GET /api/v1/admin/users/exists?username=`, and that controller is super-admin only and is not
 * proxied by tenancy. So a taken username is discovered by the 409 the create returns, which
 * `applyFieldErrors` binds onto the field. See lessons.md, "Users — a taken username cannot be
 * checked before submitting".
 */
@Injectable({providedIn: 'root'})
export class UserFormService {
  private readonly fb = inject(NonNullableFormBuilder);

  /**
   * The create/edit form.
   *
   * The password pair exists only when creating: `PUT …/update` does not change a password, and
   * offering the field on an edit would imply it did. Resetting one is its own form below, because
   * it is its own endpoint and its own permission.
   */
  build(mode: 'create' | 'edit'): UserForm {
    const form: UserForm = this.fb.group(
      {
        userName: this.fb.control('', [Validators.required, Validators.pattern(USERNAME_PATTERN)]),
        firstName: this.fb.control(''),
        lastName: this.fb.control(''),
        emailAddress: this.fb.control('', [Validators.required, Validators.email]),
        active: this.fb.control(true),
        roles: this.fb.control<readonly string[]>([], [Validators.required]),
        password: this.fb.control(''),
        repeatPassword: this.fb.control(''),
      },
      /*
       * Group-level, so the confirmation is compared rather than merely present. seller-ui's
       * change-password form passed this as `validator` — singular — which Angular's
       * `AbstractControlOptions` does not read, so its mismatch error never fired and its template
       * checked for one that could not exist.
       */
      {validators: mode === 'create' ? [passwordsMatch] : []},
    );

    if (mode === 'create') {
      form.controls.password.setValidators([Validators.required, Validators.pattern(PASSWORD_PATTERN)]);
      form.controls.repeatPassword.setValidators([Validators.required]);
    } else {
      /*
       * Disabled rather than merely untouched: `form.value` omits disabled controls, so there is no
       * route by which an edit sends a password the update endpoint would ignore anyway.
       */
      form.controls.password.disable();
      form.controls.repeatPassword.disable();
      /*
       * The username is uaa's unique key and `updateUser` never passes a new one through, so an
       * editable field here would look like a rename and silently do nothing.
       */
      form.controls.userName.disable();
    }

    form.controls.password.updateValueAndValidity();
    form.controls.repeatPassword.updateValueAndValidity();
    return form;
  }

  /**
   * Fills the edit form from a row.
   *
   * The row is what the list already returned, so nothing is re-fetched — and it could not be: an
   * org-level account is refused by `find-one` under any store, so a detail read would fail for
   * exactly the users a list can most easily be pointed at.
   */
  patchFrom(form: UserForm, row: TeamRow): void {
    form.reset({
      userName: row.userName,
      firstName: row.firstName,
      lastName: row.lastName,
      emailAddress: row.email,
      active: row.active,
      roles: row.roles,
      password: '',
      repeatPassword: '',
    });
  }
}
