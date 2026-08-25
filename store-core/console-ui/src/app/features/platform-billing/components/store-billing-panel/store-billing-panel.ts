import {Component, ElementRef, effect, inject, input, output, viewChild} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {Badge} from '@shared/ui/badge/badge';
import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {ConfirmDialog} from '@shared/ui/confirm-dialog/confirm-dialog';
import {EmptyState} from '@shared/ui/empty-state/empty-state';
import {Icon} from '@shared/ui/icon/icon';
import {LoadError} from '@shared/ui/load-error/load-error';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {Select} from '@shared/ui/select/select';
import {StoreBillingFacade} from '../../facades/store-billing.facade';

/**
 * One store's billing, in a modal, with the three levers an operator has over it.
 *
 * **Reached from three places** — a subscription row, an invoice row, and an organization's Stores
 * tab — which is why it is a component rather than a route: it is the same answer to the same
 * question asked from wherever the question comes up, and `lessons.md` recorded the Stores tab as
 * where it actually gets asked.
 *
 * **A dialog rather than a rail**, for the reason the customers panel is one: the registers behind
 * it are wide tables, and a permanent third of the width would push `app-data-table` into its
 * stacked-card fallback for a panel that is empty most of the time.
 *
 * Copy arrives through `*transloco` inside the dialog element rather than around it — a structural
 * directive wrapping the `<dialog>` defers the embedded view past the constructor's effect, and
 * `viewChild.required` then throws NG0951 before the element exists.
 */
@Component({
  selector: 'app-store-billing-panel',
  imports: [Badge, BusyOverlay, ConfirmDialog, EmptyState, Icon, LoadError, NoticeBar, Select, TranslocoDirective],
  providers: [StoreBillingFacade],
  templateUrl: './store-billing-panel.html',
  styleUrls: ['./store-billing-panel.css', '../../../../shared/styles/dialog-motion.css'],
})
export class StoreBillingPanel {
  protected readonly facade = inject(StoreBillingFacade);

  /** The store to read. Null closes the panel; a change re-reads it. */
  readonly store = input<string | null>(null);

  readonly dismissed = output<void>();

  /**
   * A write landed.
   *
   * The panel reloads itself; this tells the register behind it to do the same, because a plan
   * change moves a row it is showing and writes an audit line it is not.
   */
  readonly changed = output<void>();

  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  constructor() {
    effect(() => this.facade.store.set(this.store()));

    effect(() => {
      const element = this.dialog().nativeElement;
      if (this.store()) {
        if (!element.open) {
          element.showModal();
        }
      } else if (element.open) {
        element.close();
      }
    });
  }

  /**
   * Escape was pressed. The parent owns which store is open, so it is what has to be told.
   *
   * **`cancel` is bound and `close` deliberately is not**, and getting that backwards cost an
   * afternoon. `close` fires for a dismissal *and* for the `element.close()` the effect above makes
   * — queued rather than synchronously, so a stale one can land after the panel has reopened for
   * another store and shut it. Trying to tell the two apart with a flag then failed the other way:
   * when the effect runs after the browser has already closed the dialog, no `close` arrives to
   * clear the flag, and the next dismissal is swallowed — leaving the parent holding a store it had
   * dismissed, so clicking that same row changed no signal and reopened nothing.
   *
   * A modal dialog has exactly two ways out: Escape, which fires `cancel`, and this component's own
   * button. Listening to those two and ignoring `close` removes the pairing, the flag and both
   * failure modes together.
   */
  protected onDismiss(): void {
    this.dismissed.emit();
  }

  /** Asks the parent to close, so the state that drives the panel leads rather than follows. */
  protected close(): void {
    this.dismissed.emit();
  }

  /*
   * Bound once as a field rather than as `onChanged.bind(this)` in the template: a method reference
   * created in a binding is a new function on every change detection, which makes the callback the
   * facade holds look different on every tick.
   */
  protected readonly notifyChanged = () => this.changed.emit();
}
