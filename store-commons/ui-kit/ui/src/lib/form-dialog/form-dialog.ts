import {Component, ElementRef, booleanAttribute, effect, input, output, viewChild} from '@angular/core';

import {Icon} from '../icon/icon';

/**
 * The modal shell an editing form sits in.
 *
 * The catalogue had three dialogs — confirm, set-password, roles — and no shell, so each carried its
 * own copy of the `<dialog>`/`showModal()` dance, the title row and the motion import. A fourth
 * screen needing a modal form is the point at which that stops being reasonable.
 *
 * **The consumer keeps its own `<form>`.** This projects content and nothing else: a reactive form
 * needs `[formGroup]` on the form element, and the action row differs per screen — a role dialog
 * saves and deletes, a user dialog also sets a password. Owning the form here would mean either
 * passing the `FormGroup` into the library or reimplementing every screen's footer. The shell is the
 * part that was duplicated; the form is not.
 *
 * ```html
 * <app-form-dialog [open]="!!facade.editing()" [title]="t('roles.form.editTitle')"
 *                  [closeLabel]="t('shared.actions.close')" (dismissed)="facade.dismiss()">
 *   <form [formGroup]="facade.form" (ngSubmit)="facade.save()">…</form>
 * </app-form-dialog>
 * ```
 */
@Component({
  selector: 'app-form-dialog',
  imports: [Icon],
  template: `
    <dialog #dialog [class.wide]="wide()" (close)="dismissed.emit()" (cancel)="dismissed.emit()">
      <div class="dialog-head">
        <div class="dialog-heading">
          <h2 class="dialog-title">{{ title() }}</h2>
          @if (description(); as text) {
            <p class="dialog-description">{{ text }}</p>
          }
        </div>
        <button
          class="dialog-close"
          type="button"
          [attr.aria-label]="closeLabel()"
          [title]="closeLabel()"
          (click)="close()"
        >
          <app-icon name="x" [size]="15" />
        </button>
      </div>

      <div class="dialog-body">
        <ng-content />
      </div>
    </dialog>
  `,
  styleUrls: ['./form-dialog.css', '../../styles/dialog-motion.css'],
})
export class FormDialog {
  /*
   * Transformed, so `<app-form-dialog open>` works. Half the catalogue's booleans carry this and
   * half do not, and the half that do not fail as `Type 'string' is not assignable to type
   * 'boolean'` from the AOT build only — never from `ng test`.
   */
  readonly open = input(false, {transform: booleanAttribute});
  readonly title = input.required<string>();
  readonly description = input<string | null>(null);
  readonly closeLabel = input.required<string>();
  /** A form with two columns or a repeating row needs more than the default 28rem. */
  readonly wide = input(false, {transform: booleanAttribute});

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
    this.dialog().nativeElement.close();
  }
}
