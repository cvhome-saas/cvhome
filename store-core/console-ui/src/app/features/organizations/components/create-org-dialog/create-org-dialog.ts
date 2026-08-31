import {Component, ElementRef, computed, effect, input, output, signal, viewChild} from '@angular/core';

import type {CreateOrgUser} from '@api/tenancy/org.service';
import {containsPersonalToken, isCommonPassword} from '@shared/validators/password-strength';
import {FormField} from '@shared/ui/form-field/form-field';
import {TextField} from '@shared/ui/text-field/text-field';

/** The server normalises the address to lowercase; this only has to reject what is not one. */
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

/**
 * The rules tenancy will actually accept, not seller-ui's.
 *
 * This dialog carried seller-ui's `PWD_PATTERN` verbatim — upper, lower, a digit, **six to twelve**
 * characters — on the argument that the platform had no policy to mirror. It has one now: this endpoint
 * shares `CreateOrgRequest` and `SignupService` with public signup, so `SignUpUser`'s constraints apply
 * here too, and a six-character password is a 400 rather than an organization. The twelve-character ceiling
 * was the worse half anyway: it is a rule no password store should have, and it forbade exactly the
 * passphrases that survive contact with an attacker.
 *
 * Composition is deliberately gone with it. `Password1!` satisfies three character classes and is on the
 * common-password list two lines below; NIST has advised against composition rules since SP 800-63B.
 */
const MIN_PASSWORD_LENGTH = 8;

const MAX_PASSWORD_LENGTH = 72;

/**
 * Creates an organization and its first administrator.
 *
 * **Why this is not `app-confirm-dialog`.** That component confirms and has no content projection;
 * this collects five values. The same split Module 7 made for the payment approval dialog and
 * Module 8 made twice over — collecting and confirming are different jobs.
 *
 * **What it deliberately does not ask for.** A *name* for the organization: signup now names one after its
 * first administrator when none is given (`SignUpUser.organizationNameOrDefault`), so the dialog says which
 * name it will get rather than offering a control for something with a sensible default. And a
 * subscription plan, which seller-ui's form offered and `CreateOrgRequest` has no field for: a plan
 * belongs to a store now, so that control was choosing something applied nowhere.
 *
 * Its copy arrives as inputs rather than through `*transloco` here: a structural directive wrapping
 * the `<dialog>` defers the embedded view past the constructor's effect, and `viewChild.required`
 * then throws NG0951 before the element exists.
 */
@Component({
  selector: 'app-create-org-dialog',
  imports: [FormField, TextField],
  templateUrl: './create-org-dialog.html',
  styleUrls: ['./create-org-dialog.css', '../../../../shared/styles/dialog-motion.css'],
})
export class CreateOrgDialog {
  readonly open = input(false);
  /** True while the POST is in flight — the dialog stays open and its buttons lock. */
  readonly busy = input(false);

  readonly title = input.required<string>();
  readonly message = input.required<string>();
  readonly firstNameLabel = input.required<string>();
  readonly lastNameLabel = input.required<string>();
  readonly emailLabel = input.required<string>();
  readonly passwordLabel = input.required<string>();
  readonly repeatLabel = input.required<string>();
  readonly passwordHint = input.required<string>();
  readonly unnamedHint = input.required<string>();
  readonly emailInvalidMessage = input.required<string>();
  readonly weakMessage = input.required<string>();
  readonly mismatchMessage = input.required<string>();
  readonly confirmLabel = input.required<string>();
  readonly busyLabel = input.required<string>();
  readonly cancelLabel = input.required<string>();

  readonly submitted = output<CreateOrgUser>();
  readonly dismissed = output<void>();

  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  protected readonly firstName = signal('');
  protected readonly lastName = signal('');
  protected readonly email = signal('');
  protected readonly password = signal('');
  protected readonly repeat = signal('');
  protected readonly touched = signal(false);

  protected readonly emailInvalid = computed(() => !EMAIL_PATTERN.test(this.email().trim()));
  protected readonly weak = computed(() => {
    const password = this.password();
    return (
      password.length < MIN_PASSWORD_LENGTH ||
      password.length > MAX_PASSWORD_LENGTH ||
      isCommonPassword(password) ||
      containsPersonalToken(password, this.firstName(), this.lastName(), this.email())
    );
  });
  protected readonly mismatched = computed(() => this.password() !== this.repeat());
  protected readonly invalid = computed(
    () => !this.firstName().trim() || !this.lastName().trim() || this.emailInvalid() || this.weak() || this.mismatched(),
  );

  /** One message at a time, in the order the operator would fix them. */
  protected readonly error = computed(() => {
    if (!this.touched()) {
      return null;
    }
    if (this.emailInvalid()) {
      return this.emailInvalidMessage();
    }
    if (this.weak()) {
      return this.weakMessage();
    }
    return this.mismatched() ? this.mismatchMessage() : null;
  });

  constructor() {
    effect(() => {
      const element = this.dialog().nativeElement;
      if (this.open()) {
        if (!element.open) {
          this.firstName.set('');
          this.lastName.set('');
          this.email.set('');
          this.password.set('');
          this.repeat.set('');
          this.touched.set(false);
          element.showModal();
        }
      } else if (element.open) {
        element.close();
      }
    });
  }

  protected onSubmit(event: Event): void {
    event.preventDefault();
    this.touched.set(true);
    if (!this.invalid() && !this.busy()) {
      this.submitted.emit({
        firstName: this.firstName().trim(),
        lastName: this.lastName().trim(),
        emailAddress: this.email().trim(),
        password: this.password(),
        repeatPassword: this.repeat(),
      });
    }
  }

  /** Asks the parent to close, so the state that drives `open` leads rather than follows. */
  protected close(): void {
    this.dismissed.emit();
  }
}
