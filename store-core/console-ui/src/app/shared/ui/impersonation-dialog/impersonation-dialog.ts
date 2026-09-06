import {Component, computed, inject, input, linkedSignal, output, signal} from '@angular/core';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

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
  private readonly transloco = inject(TranslocoService);

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

  /** The chosen account's label, for a submit that names what it does. */
  protected readonly targetLabel = computed(
    () => this.targets().find((target) => target.value === this.targetId())?.label ?? '',
  );

  protected readonly canSubmit = computed(
    () => !this.busy() && !!this.targetId() && !!this.storeId() && this.reason().trim().length > 0,
  );

  /**
   * Computed rather than written as an array literal in the template: a literal is a new array on every
   * change-detection pass, and the select closes its popover when its options change identity.
   */
  protected readonly modeOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    const read = {value: 'read', label: this.transloco.translate('platform.impersonation.mode.read')};
    const write = {value: 'write', label: this.transloco.translate('platform.impersonation.mode.write')};
    return this.allowWrite() ? [read, write] : [read];
  });

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
