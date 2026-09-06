import {Component, computed, input, linkedSignal, output, signal} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import type {StartImpersonation} from '@models/impersonation';
import {FormDialog, Select, type SelectOption, TextareaField} from '@cvhome-saas/ui-kit/ui';

/** How much of a reason the audit row keeps. */
const REASON_MAX = 200;

/**
 * Who to act as, in which store, how, and why.
 *
 * One dialog for both ways in. From an account row the target is fixed and the stores are the
 * ones that account acts in; from a store row the store is fixed and the targets are the accounts
 * that act in it. A choice with one option renders as that option, already picked.
 *
 * **Read-only is the default** and write mode is offered only where the host says the operator may
 * have it — uaa refuses it to support, and a control that always answers 403 is worse than none.
 * The reason is required: an impersonation nobody can review is the one that has to be switched
 * off again.
 */
@Component({
  selector: 'app-impersonation-dialog',
  imports: [FormDialog, Select, TextareaField, TranslocoDirective],
  templateUrl: './impersonation-dialog.html',
  styleUrl: './impersonation-dialog.css',
})
export class ImpersonationDialog {
  readonly open = input(false);
  readonly busy = input(false);
  /** The accounts that may be acted as. One means it is fixed. */
  readonly targets = input.required<readonly SelectOption[]>();
  /** The stores the impersonation may enter. One means it is fixed. */
  readonly stores = input.required<readonly SelectOption[]>();
  /** Whether write mode is on offer at all. */
  readonly allowWrite = input(false);

  readonly submitted = output<StartImpersonation>();
  readonly dismissed = output<void>();

  protected readonly targetId = linkedSignal(() => this.targets()[0]?.value ?? '');
  protected readonly storeId = linkedSignal(() => this.stores()[0]?.value ?? '');
  protected readonly mode = signal<'read' | 'write'>('read');
  protected readonly reason = signal('');

  protected readonly reasonMax = REASON_MAX;

  protected readonly canSubmit = computed(
    () => !this.busy() && !!this.targetId() && !!this.storeId() && this.reason().trim().length > 0,
  );

  protected readonly modeOptions = computed<readonly SelectOption[]>(() =>
    this.allowWrite() ? [{value: 'read', label: ''}, {value: 'write', label: ''}] : [{value: 'read', label: ''}],
  );

  protected onSubmit(event: Event): void {
    event.preventDefault();
    if (!this.canSubmit()) {
      return;
    }
    this.submitted.emit({
      userId: this.targetId(),
      storeId: this.storeId(),
      mode: this.mode(),
      reason: this.reason().trim(),
    });
  }

  protected setMode(value: string): void {
    this.mode.set(value === 'write' ? 'write' : 'read');
  }

  protected reset(): void {
    this.mode.set('read');
    this.reason.set('');
    this.dismissed.emit();
  }
}
