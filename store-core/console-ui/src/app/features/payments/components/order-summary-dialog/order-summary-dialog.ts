import {Component, ElementRef, effect, input, output, viewChild} from '@angular/core';
import {TranslocoDatePipe} from '@jsverse/transloco-locale';

import type {OrderSummary} from '@models/transactions';
import {Badge} from '@shared/ui/badge/badge';
import {Icon} from '@shared/ui/icon/icon';

/**
 * The order behind a transaction, read without leaving the ledger.
 *
 * Deliberately **not** the detail page in a box. No status composer, no invoice, no address panels,
 * no payment block — the operator is looking at the payment already, and reproducing the page they
 * can reach anyway would make this a slower route to the same place. It answers one question, "what
 * did this pay for": who bought, what, how many, and what it came to.
 *
 * There are no actions on it for the same reason. The only control is Close.
 *
 * Its copy arrives as inputs rather than through `*transloco` in this template, following
 * `approve-dialog`: a structural directive around the `<dialog>` defers the embedded view past the
 * constructor's effect, and `viewChild.required` then throws NG0951 before the element exists.
 */
@Component({
  selector: 'app-order-summary-dialog',
  imports: [Badge, Icon, TranslocoDatePipe],
  templateUrl: './order-summary-dialog.html',
  styleUrl: './order-summary-dialog.css',
})
export class OrderSummaryDialog {
  readonly open = input(false);
  readonly summary = input<OrderSummary | null>(null);
  readonly loading = input(false);
  readonly error = input<Error | undefined>(undefined);

  /** The order reference, known before the fetch lands, so the heading does not appear late. */
  readonly reference = input<string>('');

  readonly title = input.required<string>();
  readonly loadingLabel = input.required<string>();
  readonly errorLabel = input.required<string>();
  readonly customerLabel = input.required<string>();
  readonly placedLabel = input.required<string>();
  readonly itemsLabel = input.required<string>();
  readonly itemsHeading = input.required<string>();
  readonly totalLabel = input.required<string>();
  readonly quantityLabel = input.required<string>();
  readonly closeLabel = input.required<string>();

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
   * Asks the parent to close, rather than closing the element and hoping the parent notices.
   *
   * The dialog is driven by `open`, so the state has to lead: emitting `dismissed` clears it and the
   * effect closes the element on the way back down. Closing imperatively here instead left the
   * parent believing the dialog was still open — `open` stayed `true`, the effect had no new value
   * to react to, and the dialog could never be opened a second time. Found in QA; it opened once
   * and then the order reference did nothing.
   *
   * `(close)` and `(cancel)` on the element stay wired for the dismissals the platform owns —
   * Escape and the backdrop — and emitting twice is harmless, since clearing a cleared signal is a
   * no-op.
   */
  protected close(): void {
    this.dismissed.emit();
  }
}
