import {Component, ElementRef, computed, effect, input, output, signal, viewChild} from '@angular/core';

import {Checkbox} from '../checkbox/checkbox';

/** The set of roles an account should hold, and the two calls needed to get there. */
export interface RoleChange {
  readonly add: readonly string[];
  readonly remove: readonly string[];
}

/** One role, with the words to render it in. */
export interface RoleOption {
  readonly value: string;
  readonly label: string;
}

/**
 * Grants and revokes an account's roles.
 *
 * **Why this is not `app-confirm-dialog`.** That component confirms; this collects a set. The same
 * split the payment approval, the invitation and the password dialogs each made — collecting and
 * confirming are different jobs.
 *
 * **Why it emits a diff rather than a set.** uaa has no "these are the roles now" endpoint: granting
 * and revoking are two paths, `POST …/roles` and `POST …/roles/remove`. Emitting the set would push
 * that subtraction into every caller; emitting the diff means the one place that knows the rule is
 * the one place that has the before and after in hand.
 *
 * Its copy arrives as inputs rather than through `*transloco` here: a structural directive wrapping
 * the `<dialog>` defers the embedded view past the constructor's effect, and `viewChild.required`
 * then throws NG0951 before the element exists.
 */
@Component({
  selector: 'app-roles-dialog',
  imports: [Checkbox],
  templateUrl: './roles-dialog.html',
  styleUrls: ['./roles-dialog.css', '../../styles/dialog-motion.css'],
})
export class RolesDialog {
  readonly open = input(false);
  readonly busy = input(false);

  readonly title = input.required<string>();
  readonly message = input.required<string>();
  readonly options = input.required<readonly RoleOption[]>();
  /** The roles the account holds now. The diff is taken against this. */
  readonly held = input.required<readonly string[]>();
  readonly emptyMessage = input.required<string>();
  readonly confirmLabel = input.required<string>();
  readonly busyLabel = input.required<string>();
  readonly cancelLabel = input.required<string>();

  readonly submitted = output<RoleChange>();
  readonly dismissed = output<void>();

  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  /** The working set, seeded from `held` when the dialog opens and not tracked to it after. */
  protected readonly selected = signal<readonly string[]>([]);

  protected readonly unchanged = computed(() => {
    const before = [...this.held()].sort();
    const after = [...this.selected()].sort();
    return before.length === after.length && before.every((role, index) => role === after[index]);
  });

  constructor() {
    effect(() => {
      const element = this.dialog().nativeElement;
      if (this.open()) {
        if (!element.open) {
          this.selected.set([...this.held()]);
          element.showModal();
        }
      } else if (element.open) {
        element.close();
      }
    });
  }

  protected holds(role: string): boolean {
    return this.selected().includes(role);
  }

  protected toggle(role: string): void {
    this.selected.update((roles) => (roles.includes(role) ? roles.filter((held) => held !== role) : [...roles, role]));
  }

  protected onSubmit(event: Event): void {
    event.preventDefault();
    if (this.busy() || this.unchanged()) {
      return;
    }
    const before = this.held();
    const after = this.selected();
    this.submitted.emit({
      add: after.filter((role) => !before.includes(role)),
      remove: before.filter((role) => !after.includes(role)),
    });
  }

  /** Asks the parent to close, so the state that drives `open` leads rather than follows. */
  protected close(): void {
    this.dismissed.emit();
  }
}
