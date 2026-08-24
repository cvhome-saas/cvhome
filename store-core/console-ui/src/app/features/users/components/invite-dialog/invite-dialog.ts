import {Component, ElementRef, computed, effect, input, output, signal, viewChild} from '@angular/core';

import {FormField} from '@shared/ui/form-field/form-field';
import {Select, type SelectOption} from '@shared/ui/select/select';
import {TextField} from '@shared/ui/text-field/text-field';

/** The server normalises the address to lowercase; this only has to reject what is not one. */
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

/**
 * Invites an address into the organization.
 *
 * **Why this is not `app-confirm-dialog`.** That component confirms and has no content projection.
 * This collects two values — an address and a role — the same split Module 7 made for the payment
 * approval dialog and this module made again for setting a password.
 *
 * Its copy arrives as inputs rather than through `*transloco` here: a structural directive wrapping
 * the `<dialog>` defers the embedded view past the constructor's effect, and `viewChild.required`
 * then throws NG0951 before the element exists.
 */
@Component({
  selector: 'app-invite-dialog',
  imports: [FormField, Select, TextField],
  templateUrl: './invite-dialog.html',
  styleUrls: ['./invite-dialog.css', '../../../../shared/styles/dialog-motion.css'],
})
export class InviteDialog {
  readonly open = input(false);
  readonly busy = input(false);

  readonly title = input.required<string>();
  readonly message = input.required<string>();
  readonly emailLabel = input.required<string>();
  readonly roleLabel = input.required<string>();
  readonly roles = input.required<readonly SelectOption[]>();
  readonly invalidMessage = input.required<string>();
  readonly noMailHint = input.required<string>();
  readonly confirmLabel = input.required<string>();
  readonly busyLabel = input.required<string>();
  readonly cancelLabel = input.required<string>();

  readonly invited = output<{email: string; role: string}>();
  readonly dismissed = output<void>();

  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  protected readonly email = signal('');
  protected readonly role = signal('');
  protected readonly touched = signal(false);

  protected readonly invalid = computed(() => !EMAIL_PATTERN.test(this.email().trim()) || !this.role());
  protected readonly showError = computed(() => this.touched() && this.invalid());

  constructor() {
    effect(() => {
      const element = this.dialog().nativeElement;
      if (this.open()) {
        if (!element.open) {
          this.email.set('');
          // The server's own default, so the pre-selection matches what omitting the parameter does.
          this.role.set(this.roles()[0]?.value ?? '');
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
      this.invited.emit({email: this.email().trim(), role: this.role()});
    }
  }

  /** Asks the parent to close, so the state that drives `open` leads rather than follows. */
  protected close(): void {
    this.dismissed.emit();
  }
}
