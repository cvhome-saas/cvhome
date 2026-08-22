import {Component, ElementRef, computed, effect, input, output, signal, viewChild} from '@angular/core';

import {FormField} from '@shared/ui/form-field/form-field';
import {TextField} from '@shared/ui/text-field/text-field';

/**
 * Confirms a payment against the external reference the operator has in front of them.
 *
 * **Why this is not `app-confirm-dialog`.** That component's typed mode asks the operator to repeat
 * a phrase the console already knows, as a speed bump before something irreversible. This asks for a
 * value the console does *not* know and cannot guess — the bank's transaction number — and sends it
 * to the server as `PaymentApprovalRequest.transactionNo`, which is `@NotBlank`. Confirming and
 * collecting are different jobs, and only this page does the second one, so it lives here rather
 * than in the shared catalogue.
 *
 * Everything else follows `confirm-dialog` deliberately: a native `<dialog>` with `showModal()`, so
 * the top layer, the focus trap, the backdrop, `Escape` and `aria-modal` come from the platform —
 * and **its copy arrives as inputs rather than through `*transloco` here**. A structural directive
 * wrapping the `<dialog>` would defer the embedded view past the constructor's effect, and
 * `viewChild.required` then throws NG0951 before the element exists.
 */
@Component({
  selector: 'app-approve-dialog',
  imports: [FormField, TextField],
  templateUrl: './approve-dialog.html',
  styleUrl: './approve-dialog.css',
})
export class ApproveDialog {
  readonly open = input(false);
  /** True while the POST is in flight — the dialog stays open and its buttons lock. */
  readonly busy = input(false);

  readonly title = input.required<string>();
  readonly message = input.required<string>();
  readonly fieldLabel = input.required<string>();
  readonly fieldHint = input.required<string>();
  readonly requiredMessage = input.required<string>();
  readonly confirmLabel = input.required<string>();
  readonly busyLabel = input.required<string>();
  readonly cancelLabel = input.required<string>();

  readonly approved = output<string>();
  readonly dismissed = output<void>();

  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  protected readonly transactionNo = signal('');
  protected readonly touched = signal(false);

  /** `@NotBlank` server-side, so whitespace alone is not a value. */
  protected readonly invalid = computed(() => this.transactionNo().trim() === '');
  protected readonly showError = computed(() => this.touched() && this.invalid());

  constructor() {
    effect(() => {
      const element = this.dialog().nativeElement;
      if (this.open()) {
        if (!element.open) {
          this.transactionNo.set('');
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
      this.approved.emit(this.transactionNo().trim());
    }
  }

  protected close(): void {
    this.dialog().nativeElement.close();
  }
}
