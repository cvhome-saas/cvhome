import {Component, ElementRef, effect, input, output, viewChild} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';

import {FieldError, FormField, NoticeBar, Select, type SelectOption, TextField} from '@cvhome-saas/ui-kit/ui';
import type {PodForm} from '../../services/pod-form.service';

/**
 * Registers a pod, or edits the two things about one that can be changed.
 *
 * **Why this is a dialog and not the panel it used to be.** Editing is an interruption of reading:
 * the page's job is to show a pod's state, routing and tenants, and a form permanently occupying the
 * foot of it made the page look like a form with facts above it. It is also the only writable thing
 * on the page, and the lifecycle levers it belongs beside are in the header.
 *
 * **Why it takes the form rather than building one.** The create route and the edit route are the
 * same fields with one difference — the owner, which is settable exactly once — and
 * `PodFormService` already encodes that by disabling the control. A dialog that built its own form
 * would be a second place that rule lives.
 *
 * Its copy arrives as inputs rather than through `*transloco` here: a structural directive wrapping
 * the `<dialog>` defers the embedded view past the constructor's effect, and `viewChild.required`
 * then throws NG0951 before the element exists.
 */
@Component({
  selector: 'app-pod-form-dialog',
  imports: [FieldError, FormField, NoticeBar, ReactiveFormsModule, Select, TextField],
  templateUrl: './pod-form-dialog.html',
  styleUrls: ['./pod-form-dialog.css', '../../../../shared/styles/dialog-motion.css'],
})
export class PodFormDialog {
  readonly open = input(false);
  readonly busy = input(false);
  /** Null while nothing is being edited; the parent owns the form's lifetime. */
  readonly form = input.required<PodForm | null>();
  /** Whether this is a registration. Decides the title, the submit label and the owner's hint. */
  readonly creating = input(false);

  readonly title = input.required<string>();
  readonly editableFields = input.required<string>();
  readonly nameLabel = input.required<string>();
  readonly nameHint = input.required<string>();
  readonly nameInvalid = input.required<string>();
  readonly endpointLabel = input.required<string>();
  readonly endpointHint = input.required<string>();
  readonly endpointInvalid = input.required<string>();
  readonly endpointTypeLabel = input.required<string>();
  readonly endpointTypes = input.required<readonly SelectOption[]>();
  readonly ownerLabel = input.required<string>();
  readonly ownerHint = input.required<string>();
  readonly ownerOptions = input.required<readonly SelectOption[]>();
  readonly confirmLabel = input.required<string>();
  readonly busyLabel = input.required<string>();
  readonly cancelLabel = input.required<string>();

  readonly submitted = output<void>();
  readonly dismissed = output<void>();

  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  constructor() {
    effect(() => {
      const element = this.dialog().nativeElement;
      if (this.open()) {
        if (!element.open) {
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
      this.submitted.emit();
    }
  }

  /** Asks the parent to close, so the state that drives `open` leads rather than follows. */
  protected close(): void {
    this.dismissed.emit();
  }
}
