import {Component, ElementRef, effect, input, output, viewChild} from '@angular/core';

import {CopyField} from '@shared/ui/copy-field/copy-field';

/**
 * The link to an invitation, shown once because it exists once.
 *
 * `CreatedInvitationDto` carries the token in plaintext in the response that created the invitation
 * and **nowhere else** — only its hash is stored, so no later read can reconstruct it and "resend"
 * is a rotation rather than a repeat. Nothing on the platform sends email either, so this dialog is
 * the entire delivery mechanism: if the operator closes it without copying the link, the only
 * recourse is to resend and get a different one.
 *
 * That is why this is a dialog of its own rather than a toast. A toast dismisses itself.
 *
 * See lessons.md, "Users — nothing emails an invitation".
 */
@Component({
  selector: 'app-invitation-link-dialog',
  imports: [CopyField],
  templateUrl: './invitation-link-dialog.html',
  styleUrl: './invitation-link-dialog.css',
})
export class InvitationLinkDialog {
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

  protected close(): void {
    this.dismissed.emit();
  }
}
