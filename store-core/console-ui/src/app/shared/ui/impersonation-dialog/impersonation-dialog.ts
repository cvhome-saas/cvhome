import {Component, computed, input, output, signal} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import type {StartImpersonation} from '@models/impersonation';
import {FormDialog, type SelectOption, TextareaField} from '@cvhome-saas/ui-kit/ui';

/** How much of a reason the audit row keeps. */
const REASON_MAX = 200;

/**
 * Who to act as, and why.
 *
 * The account is fixed by the row the dialog opened from, and the session will be that account — its
 * own org, stores and roles, whatever they are — so there is nothing else to choose. The reason is
 * required: an impersonation nobody can review is the one that has to be switched off again.
 */
@Component({
  selector: 'app-impersonation-dialog',
  imports: [FormDialog, TextareaField, TranslocoDirective],
  templateUrl: './impersonation-dialog.html',
  styleUrl: './impersonation-dialog.css',
})
export class ImpersonationDialog {
  readonly open = input(false);
  readonly busy = input(false);
  /** The account to act as: its id, and the label the confirm button names. */
  readonly target = input<SelectOption | null>(null);

  readonly submitted = output<StartImpersonation>();
  readonly dismissed = output<void>();

  protected readonly reason = signal('');

  protected readonly reasonMax = REASON_MAX;

  protected readonly canSubmit = computed(
    () => !this.busy() && !!this.target() && this.reason().trim().length > 0,
  );

  protected onSubmit(event: Event): void {
    event.preventDefault();
    const target = this.target();
    if (!target || !this.canSubmit()) {
      return;
    }
    this.submitted.emit({userId: target.value, reason: this.reason().trim()});
  }

  protected reset(): void {
    this.reason.set('');
    this.dismissed.emit();
  }
}
