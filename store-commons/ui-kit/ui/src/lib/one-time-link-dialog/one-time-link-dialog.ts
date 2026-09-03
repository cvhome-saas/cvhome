import {Component, ElementRef, effect, input, output, viewChild} from '@angular/core';

import {CopyField} from '../copy-field/copy-field';

/**
 * A link that is shown once because it exists once.
 *
 * Both consoles issue one-time links whose token is stored only as a hash: tenancy's store-member
 * invitations, and uaa's invitations and admin-issued password resets. The response that created
 * the link is the only place the plaintext ever appears — no later read can reconstruct it, and
 * "resend" is a rotation rather than a repeat. Nothing on the platform emails it either: delivery
 * is an outbox event a future service may pick up, and until then the operator carrying the link
 * *is* the delivery mechanism. If they close this without copying, the only recourse is a new link.
 *
 * That is why it is a dialog of its own rather than a toast. A toast dismisses itself.
 *
 * Moved here from console-ui's `invitation-link-dialog` when uaa's console needed the same thing
 * for two more kinds of link; the copy arrives as inputs so each caller names its own kind.
 */
@Component({
  selector: 'app-one-time-link-dialog',
  imports: [CopyField],
  templateUrl: './one-time-link-dialog.html',
  styleUrls: ['./one-time-link-dialog.css', '../../styles/dialog-motion.css'],
})
export class OneTimeLinkDialog {
  readonly open = input(false);

  readonly title = input.required<string>();
  readonly message = input.required<string>();
  readonly linkLabel = input.required<string>();
  readonly link = input.required<string>();
  readonly warning = input.required<string>();
  readonly doneLabel = input.required<string>();

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

  /**
   * Asks the parent to close rather than closing the element itself: the dialog is driven by
   * `open`, so the state has to lead, or it can never be opened a second time.
   */
  protected close(): void {
    this.dismissed.emit();
  }
}
