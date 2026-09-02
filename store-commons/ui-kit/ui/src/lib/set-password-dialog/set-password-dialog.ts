import {Component, ElementRef, computed, effect, input, output, signal, viewChild} from '@angular/core';

import {FormField} from '../form-field/form-field';
import {TextField} from '../text-field/text-field';

/** Upper, lower, a digit, eight or more. The console's rule, because the platform has none. */
const PASSWORD_PATTERN = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d).{8,}$/;

/**
 * Sets another user's password.
 *
 * In `shared/ui/` because three screens set one: the store team (Module 8, where it was written),
 * the platform account list, and an organization's owner. It was a page-local component on the
 * argument that only one page set a password; that stopped being true.
 *
 * **Why this is not `app-confirm-dialog`.** That component confirms; it has no content projection
 * and its typed mode asks the operator to repeat a phrase the console already knows. This *collects*
 * two values the console does not know and compares them. Confirming and collecting are different
 * jobs — the same split Module 7 made for the payment approval dialog, and for the same reason it
 * lives in the feature rather than in the shared catalogue: only this page sets a password.
 *
 * Two things this deliberately does not ask for.
 *
 * **The current password.** `UserPassword` has a field for it and
 * `UserAccountServiceImpl.changePassword` reads only the new one, so nothing on the platform would
 * verify what was typed. Collecting it would be theatre.
 *
 * **Whether to email it.** Nothing on the platform sends mail, so the operator has to hand the
 * password over themselves, and the dialog says so rather than leaving them to wonder.
 *
 * Its copy arrives as inputs rather than through `*transloco` here: a structural directive wrapping
 * the `<dialog>` defers the embedded view past the constructor's effect, and `viewChild.required`
 * then throws NG0951 before the element exists. Learned on the approve dialog.
 */
@Component({
  selector: 'app-set-password-dialog',
  imports: [FormField, TextField],
  templateUrl: './set-password-dialog.html',
  styleUrls: ['./set-password-dialog.css', '../../styles/dialog-motion.css'],
})
export class SetPasswordDialog {
  readonly open = input(false);
  /** True while the POST is in flight — the dialog stays open and its buttons lock. */
  readonly busy = input(false);

  readonly title = input.required<string>();
  readonly message = input.required<string>();
  readonly passwordLabel = input.required<string>();
  readonly repeatLabel = input.required<string>();
  readonly hint = input.required<string>();
  readonly handoverHint = input.required<string>();
  readonly weakMessage = input.required<string>();
  readonly mismatchMessage = input.required<string>();
  readonly confirmLabel = input.required<string>();
  readonly busyLabel = input.required<string>();
  readonly cancelLabel = input.required<string>();

  readonly submitted = output<string>();
  readonly dismissed = output<void>();

  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  protected readonly password = signal('');
  protected readonly repeat = signal('');
  protected readonly touched = signal(false);

  protected readonly weak = computed(() => !PASSWORD_PATTERN.test(this.password()));
  protected readonly mismatched = computed(() => this.password() !== this.repeat());
  protected readonly invalid = computed(() => this.weak() || this.mismatched());

  protected readonly error = computed(() => {
    if (!this.touched()) {
      return null;
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
      this.submitted.emit(this.password());
    }
  }

  /**
   * Asks the parent to close rather than closing the element itself.
   *
   * The dialog is driven by `open`, so the state has to lead — closing imperatively leaves the
   * parent believing it is still open and the dialog can never be opened a second time.
   */
  protected close(): void {
    this.dismissed.emit();
  }
}
