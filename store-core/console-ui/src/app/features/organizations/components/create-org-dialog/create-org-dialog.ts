import {Component, ElementRef, computed, effect, input, output, signal, viewChild} from '@angular/core';

import type {CreateOrgUser} from '@api/tenancy/org.service';
import {FormField} from '@shared/ui/form-field/form-field';
import {TextField} from '@shared/ui/text-field/text-field';

/** The server normalises the address to lowercase; this only has to reject what is not one. */
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

/**
 * seller-ui's `PWD_PATTERN`, carried over verbatim: upper, lower, a digit, six to twelve characters.
 *
 * **Six to twelve is seller-ui's rule and not a policy**, because the platform has none —
 * `AdminService.resetPassword` accepts whatever it is given and uaa validates nothing. Module 8's
 * own reset dialog uses eight-or-more with the same three classes, and the two disagree at the
 * bottom end. Reproduced rather than reconciled here so that an organization created in this
 * console can be created in seller-ui's too, which is what parity testing needs; the twelve-character
 * ceiling in particular is a rule no password store should have. See lessons.md, "Users — no
 * password policy anywhere".
 */
const PASSWORD_PATTERN = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d).{6,12}$/;

/**
 * Creates an organization and its first administrator.
 *
 * **Why this is not `app-confirm-dialog`.** That component confirms and has no content projection;
 * this collects five values. The same split Module 7 made for the payment approval dialog and
 * Module 8 made twice over — collecting and confirming are different jobs.
 *
 * **What it deliberately does not ask for.** A *name* for the organization, because
 * `ManagerOrgEntity.createOrgFromUser` sets none and `rename` on the detail page is the only writer
 * — the dialog says the organization will appear under its contact email until it is named. And a
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
  protected readonly weak = computed(() => !PASSWORD_PATTERN.test(this.password()));
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
      });
    }
  }

  /** Asks the parent to close, so the state that drives `open` leads rather than follows. */
  protected close(): void {
    this.dismissed.emit();
  }
}
