import {Component, ElementRef, effect, input, output, viewChild} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';
import {TranslocoDatePipe} from '@jsverse/transloco-locale';

import {Badge} from '@shared/ui/badge/badge';
import {EmptyState} from '@shared/ui/empty-state/empty-state';
import {Icon} from '@shared/ui/icon/icon';
import {LoadError} from '@shared/ui/load-error/load-error';
import type {CustomerOrderRow, CustomerRow} from '@models/customers';
import type {Tone} from '@models/ui';

/**
 * One customer — read, in a modal.
 *
 * **A dialog rather than a detail rail**, for the reason `/users` moved to one: a rail is the
 * pattern for a long list you scan and drill into, and it earns its share of the page by being full
 * most of the time. Here it would be empty on arrival, permanently a third of the width, and would
 * squeeze the table hard enough that `app-data-table` drops to stacked cards.
 *
 * **Everything on it is read-only, because everything about a customer is.** There is no create,
 * update or delete endpoint for a customer anywhere in checkout, and nothing on the platform sends
 * mail, so the design's Edit details and Email actions are not drawn. See lessons.md, "Customers —
 * no write endpoint and no mail service".
 *
 * Copy arrives as inputs rather than through `*transloco` here — a structural directive wrapping
 * the `<dialog>` defers the embedded view past the constructor's effect, and `viewChild.required`
 * then throws NG0951 before the element exists.
 */
@Component({
  selector: 'app-customer-dialog',
  imports: [Badge, EmptyState, Icon, LoadError, TranslocoDatePipe, TranslocoDirective],
  templateUrl: './customer-dialog.html',
  styleUrl: './customer-dialog.css',
})
export class CustomerDialog {
  readonly open = input(false);
  readonly customer = input<CustomerRow | null>(null);

  readonly orders = input<readonly CustomerOrderRow[]>([]);
  readonly ordersLoading = input(false);
  readonly ordersError = input<Error | undefined>(undefined);
  /** The exact number of orders this customer has placed, or null before the panel has loaded. */
  readonly orderCount = input<string>('—');

  readonly statusLabel = input.required<(status: string | undefined) => string>();
  readonly statusTone = input.required<(status: string | undefined) => Tone>();

  readonly openOrder = output<number>();
  readonly viewAllOrders = output<void>();
  readonly retryOrders = output<void>();
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

  /** Asks the parent to close, so the state that drives `open` leads rather than follows. */
  protected close(): void {
    this.dismissed.emit();
  }
}
