import {Injectable, inject} from '@angular/core';
import {NonNullableFormBuilder, Validators} from '@angular/forms';

import {notACommonPassword, passwordIsNotPersonal} from '@shared/validators/password-strength';
import {passwordsMatch} from '@shared/validators/passwords-match';

/**
 * The shortest password the console will submit.
 *
 * This is not a mirror of a server rule — it is the only rule. See `SignUpFormService` below.
 */
export const MIN_PASSWORD_LENGTH = 8;

/**
 * The longest password worth accepting.
 *
 * uaa hashes with `PasswordEncoderFactories.createDelegatingPasswordEncoder()`, whose default is bcrypt, and
 * bcrypt ignores everything past 72 bytes. A longer password is not stronger, it is silently truncated — so
 * the form says so rather than letting someone believe in the other 40 characters they typed. (The limit is
 * bytes, not characters, so a password written in Arabic can still be truncated below this count. Truncation
 * is consistent, so it costs strength, not access.)
 */
export const MAX_PASSWORD_LENGTH = 72;

/** uaa's `users.first_name` / `users.last_name` are `varchar(50)`. */
export const MAX_NAME_LENGTH = 50;

/**
 * The longest address signup can actually store — **50**, not 254.
 *
 * `tenancy.manager_org.email` is `varchar(50)` and `SignupServiceImpl.createOrgUser` inserts the organization
 * *first*, so it is the binding limit even though uaa's own columns are `varchar(190)` (username) and
 * `varchar(254)` (email). Past 50 the insert fails with a `DataIntegrityViolationException`, which
 * `DataIntegrityErrorHandler` answers as `409 COMMON.DATA_INTEGRITY_VIOLATION` with no `fieldErrors[]` — which
 * is indistinguishable from a duplicate address, so `AuthFacade.bindTakenEmail` tells the visitor their address
 * is already registered. Capping here is what stops a long address from producing a message that is not merely
 * unhelpful but false. See lessons.md, "Auth — signup's field limits are the database's, and only the console
 * knows them".
 */
export const MAX_EMAIL_LENGTH = 50;

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
 *
 * So the rules are all here, and they are of two kinds:
 *
 * - **What the database will accept** — the three maximums above, each read off a column. Exceeding one does
 *   not produce a validation failure on the server, it produces a conflict the visitor cannot act on.
 * - **What a first administrator's password should be** — a minimum, a screen for the obvious ones, and a rule
 *   against putting the account's own name in it. This account owns the organization and, until a password
 *   reset exists, is unrecoverable: there is no administrator above the first one.
 *
 * `SignUp.submit` trims the three text fields before this group is asked whether it is valid, which is what
 * makes `Validators.required` reject a name of spaces.
 */
@Injectable({providedIn: 'root'})
export class SignUpFormService {
  private readonly fb = inject(NonNullableFormBuilder);

  create() {
    return this.fb.group({
      user: this.fb.group(
        {
          firstName: ['', [Validators.required, Validators.maxLength(MAX_NAME_LENGTH)]],
          lastName: ['', [Validators.required, Validators.maxLength(MAX_NAME_LENGTH)]],
          emailAddress: [
            '',
            [Validators.required, Validators.email, Validators.maxLength(MAX_EMAIL_LENGTH)],
          ],
          password: [
            '',
            [
              Validators.required,
              Validators.minLength(MIN_PASSWORD_LENGTH),
              Validators.maxLength(MAX_PASSWORD_LENGTH),
              notACommonPassword,
            ],
          ],
          repeatPassword: ['', Validators.required],
        },
        {validators: [passwordsMatch, passwordIsNotPersonal]},
      ),
    });
  }
}
