import {Component, inject, input, signal} from '@angular/core';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {Icon} from '@shared/ui/icon/icon';
import {copyText} from '@cvhome-saas/ui-kit';
import {ToastService} from '@shared/ui/toast/toast';

/**
 * A read-only monospace value with a copy button — a DNS record, an OAuth callback URL, a
 * webhook endpoint.
 *
 * Not an input: these are values the server decides and the operator carries elsewhere, so
 * they are text plus one action rather than a field that looks editable and is not.
 */
@Component({
  selector: 'app-copy-field',
  imports: [Icon, TranslocoDirective],
  template: `
    <div class="copy-field" *transloco="let t">
      <code>{{ value() }}</code>
      <button
        type="button"
        [attr.aria-label]="t('shared.copyField.copy', {what: label() ?? t('shared.copyField.defaultLabel')})"
        [attr.title]="t('shared.copyField.copy', {what: label() ?? t('shared.copyField.defaultLabel')})"
        (click)="copy()"
      >
        <app-icon [name]="copied() ? 'check' : 'copy'" />
      </button>
    </div>
  `,
  styleUrl: './copy-field.css',
})
export class CopyField {
  private readonly transloco = inject(TranslocoService);
  private readonly toast = inject(ToastService);

  readonly value = input.required<string>();
  /** What the value is, for the copy button's label. */
  readonly label = input<string | null>(null);

  /** Shows the tick in place of the clipboard mark for a moment after a successful copy. */
  protected readonly copied = signal(false);

  /**
   * Puts the value on the clipboard.
   *
   * **Success is silent, failure is not.** This used to raise a toast either way, while
   * `app-secret-field` — the console's other copy affordance — marked success with an inline tick
   * and said nothing. Two feedback patterns for one action, and the noisier one was on the control
   * used in tables, where copying several references in a row stacked a toast per press. The tick
   * is the pattern that survives: it is local to the button, it cannot pile up, and it does not
   * announce something the operator just did on purpose.
   *
   * A **failure** still gets a toast, because that is the case the operator cannot see for
   * themselves — the value is not on the clipboard and they need to know before pasting.
   *
   * `copyText` falls back to a selection-based copy where `navigator.clipboard` is missing, which
   * it always is over plain HTTP on a named host.
   */
  protected copy(): void {
    void copyText(this.value()).then((copied) => {
      if (copied) {
        this.copied.set(true);
        setTimeout(() => this.copied.set(false), COPIED_MS);
        return;
      }
      const what = this.label() ?? this.transloco.translate('shared.copyField.defaultLabel');
      this.toast.warning(this.transloco.translate('shared.copyField.failed', {what}));
    });
  }
}

/** How long the copy button shows its tick before going back to the clipboard mark. Matches `secret-field`. */
const COPIED_MS = 1600;
