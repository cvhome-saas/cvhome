import {Component, ElementRef, effect, input, output, signal, viewChild} from '@angular/core';

/**
 * A modal confirmation for an action that cannot be undone.
 *
 * Built on the native `<dialog>` element rather than a floating `<div>`: `showModal()` brings the
 * top layer, the focus trap, the backdrop, `Escape`, and `aria-modal` with it, all of which a
 * hand-rolled overlay has to reimplement and usually gets wrong. The template has no modal
 * anywhere, so there was no design to follow — this is the console's own.
 *
 * For a genuinely destructive action, pass `confirmationText`: the confirm button stays disabled
 * until the operator types that exact string. Clicking through a dialog is muscle memory; typing a
 * store's name is not.
 *
 * ```html
 * <app-confirm-dialog
 *   [open]="isConfirming()"
 *   [title]="t('storeSettings.details.deleteStore')"
 *   [confirmationText]="storeName()"
 *   (confirmed)="deleteStore()"
 *   (dismissed)="isConfirming.set(false)"
 * />
 * ```
 */
@Component({
  selector: 'app-confirm-dialog',
  template: `
    <dialog #dialog class="confirm" (close)="dismissed.emit()" (cancel)="dismissed.emit()">
      <form method="dialog" class="confirm-body" (submit)="onSubmit($event)">
        <h2 class="confirm-title">{{ title() }}</h2>
        @if (message()) {
          <p class="confirm-message">{{ message() }}</p>
        }

        @if (confirmationText(); as phrase) {
          <label class="confirm-prompt" [attr.for]="'confirm-phrase'">
            {{ confirmationLabel() }}
            <strong dir="auto">{{ phrase }}</strong>
          </label>
          <input
            id="confirm-phrase"
            class="confirm-input"
            type="text"
            autocomplete="off"
            dir="auto"
            [value]="typed()"
            (input)="onTyped($event)"
          />
        }

        <div class="confirm-actions">
          <button class="confirm-cancel" type="button" (click)="close()">{{ cancelLabel() }}</button>
          <button class="confirm-accept" type="submit" [disabled]="!canConfirm()">
            {{ confirmLabel() }}
          </button>
        </div>
      </form>
    </dialog>
  `,
  styleUrls: ['./confirm-dialog.css', '../../styles/dialog-motion.css'],
})
export class ConfirmDialog {
  readonly open = input(false);
  readonly title = input.required<string>();
  readonly message = input<string | null>(null);
  readonly confirmLabel = input.required<string>();
  readonly cancelLabel = input.required<string>();
  /** When set, the operator must type this exactly before confirming becomes possible. */
  readonly confirmationText = input<string | null>(null);
  readonly confirmationLabel = input('');

  readonly confirmed = output<void>();
  readonly dismissed = output<void>();

  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  protected typed = signal('');

  constructor() {
    effect(() => {
      const element = this.dialog().nativeElement;
      if (this.open()) {
        this.typed.set('');
        if (!element.open) {
          element.showModal();
        }
      } else if (element.open) {
        element.close();
      }
    });
  }

  protected canConfirm(): boolean {
    const phrase = this.confirmationText();
    return phrase === null || this.typed().trim() === phrase.trim();
  }

  protected onTyped(event: Event): void {
    this.typed.set((event.target as HTMLInputElement).value);
  }

  /**
   * The form's `method="dialog"` would close and emit `close` on its own, which reads as a
   * dismissal. Confirming has to be distinguishable from cancelling, so the submit is taken over.
   */
  protected onSubmit(event: Event): void {
    event.preventDefault();
    if (this.canConfirm()) {
      this.confirmed.emit();
    }
  }

  protected close(): void {
    this.dialog().nativeElement.close();
  }
}
