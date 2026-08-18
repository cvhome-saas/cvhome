import {Component, inject, input} from '@angular/core';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {Icon} from '@shared/ui/icon/icon';
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
        <app-icon name="copy" />
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

  protected copy(): void {
    const what = this.label() ?? this.transloco.translate('shared.copyField.defaultLabel');
    /*
     * The Clipboard API is absent on insecure origins, and rejects when the document is not
     * focused. Saying so beats a button that silently does nothing.
     */
    if (!navigator.clipboard) {
      this.toast.warning(this.transloco.translate('shared.copyField.failed', {what}));
      return;
    }
    navigator.clipboard.writeText(this.value()).then(
      () => this.toast.success(this.transloco.translate('shared.copyField.copied', {what})),
      () => this.toast.warning(this.transloco.translate('shared.copyField.failed', {what})),
    );
  }
}
