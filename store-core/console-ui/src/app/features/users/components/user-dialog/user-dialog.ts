import {Component, ElementRef, effect, input, output, viewChild} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {Badge, Checkbox, FieldError, FormField, Icon, TextField, Toggle} from '@cvhome-saas/ui-kit/ui';
import type {TeamRow} from '@models/team';
import type {UserForm} from '../../services/user-form.service';

/**
 * One account — read, or edited, in a modal.
 *
 * **Why a dialog rather than the detail rail this page used to carry.** A rail is the pattern for a
 * long list you scan and drill into, and it earns its half of the page by being full most of the
 * time. A team is a handful of people: the rail was empty on arrival, permanently took a third of
 * the width, and squeezed the table hard enough that it dropped to stacked cards. Reading or editing
 * one account is a discrete task with a commit at the end, which is exactly what a modal is for —
 * and it gives the table the whole page back.
 *
 * One dialog, two modes, because the actions belong to the record: an operator who opens someone to
 * check their roles is one click from changing them, without the record moving on screen.
 *
 * Copy arrives as inputs rather than through `*transloco` here — a structural directive wrapping
 * the `<dialog>` defers the embedded view past the constructor's effect, and `viewChild.required`
 * then throws NG0951 before the element exists.
 */
@Component({
  selector: 'app-user-dialog',
  imports: [Badge, Checkbox, FieldError, FormField, Icon, ReactiveFormsModule, TextField, Toggle, TranslocoDirective],
  templateUrl: './user-dialog.html',
  styleUrls: ['./user-dialog.css', '../../../../shared/styles/dialog-motion.css'],
})
export class UserDialog {
  readonly open = input(false);
  readonly busy = input(false);
  /** `null` while reading, a form while creating or editing. */
  readonly form = input<UserForm | null>(null);
  readonly mode = input<'view' | 'edit' | 'create'>('view');
  readonly user = input<TeamRow | null>(null);
  readonly storeName = input('');
  readonly roles = input<readonly string[]>([]);
  readonly canManage = input(false);

  readonly roleLabel = input.required<(role: string) => string>();
  readonly roleList = input.required<(roles: readonly string[]) => string>();
  readonly initials = input.required<(row: TeamRow) => string>();
  readonly hasRole = input.required<(role: string) => boolean>();

  readonly edit = output<void>();
  readonly save = output<void>();
  readonly cancelEdit = output<void>();
  readonly toggleRole = output<string>();
  readonly setActive = output<boolean>();
  readonly resetPassword = output<TeamRow>();
  readonly toggleActive = output<TeamRow>();
  readonly remove = output<TeamRow>();
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

  protected onSubmit(event: Event): void {
    event.preventDefault();
    this.save.emit();
  }
}
