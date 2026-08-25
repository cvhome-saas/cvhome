import {Component, ElementRef, effect, input, output, signal, viewChild} from '@angular/core';

import {FormField} from '@shared/ui/form-field/form-field';
import {TextareaField} from '@shared/ui/textarea-field/textarea-field';

/**
 * Suspends an organization, and records why.
 *
 * **Why this is not `app-confirm-dialog`.** That component confirms and has no content projection.
 * This confirms *and* collects the one optional value the endpoint takes — `reason`, which
 * `OrgLifecycleService` writes into the `tenancy_audit` row. Given the blast radius (every store the
 * organization owns stops being usable), the audit row is the part someone reads afterwards, and a
 * dialog that could not carry the operator's words would leave it saying "suspended by operator".
 *
 * The reason is genuinely optional: the server defaults it, and an empty box is not sent, so the
 * default is what lands rather than an empty string presented as a stated reason.
 *
 * Its copy arrives as inputs rather than through `*transloco` here: a structural directive wrapping
 * the `<dialog>` defers the embedded view past the constructor's effect, and `viewChild.required`
 * then throws NG0951 before the element exists.
 */
@Component({
  selector: 'app-suspend-org-dialog',
  imports: [FormField, TextareaField],
  templateUrl: './suspend-org-dialog.html',
  styleUrls: ['./suspend-org-dialog.css', '../../../../shared/styles/dialog-motion.css'],
})
export class SuspendOrgDialog {
  readonly open = input(false);
  readonly busy = input(false);

  readonly title = input.required<string>();
  readonly message = input.required<string>();
  readonly consequence = input.required<string>();
  readonly reasonLabel = input.required<string>();
  readonly reasonHint = input.required<string>();
  readonly confirmLabel = input.required<string>();
  readonly busyLabel = input.required<string>();
  readonly cancelLabel = input.required<string>();

  readonly submitted = output<string>();
  readonly dismissed = output<void>();

  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  protected readonly reason = signal('');

  constructor() {
    effect(() => {
      const element = this.dialog().nativeElement;
      if (this.open()) {
        if (!element.open) {
          this.reason.set('');
          element.showModal();
        }
      } else if (element.open) {
        element.close();
      }
    });
  }

  protected onSubmit(event: Event): void {
    event.preventDefault();
    if (!this.busy()) {
      this.submitted.emit(this.reason().trim());
    }
  }

  /** Asks the parent to close, so the state that drives `open` leads rather than follows. */
  protected close(): void {
    this.dismissed.emit();
  }
}
