import {Component, computed, inject, input, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import {Icon} from '@shared/ui/icon/icon';

/**
 * A stored credential: masked, revealable, copyable, editable.
 *
 * Written for the one situation the console actually has, which is not the usual one. The endpoints
 * behind this decrypt before serialising, so the browser already holds the real Stripe secret key,
 * webhook secret and OAuth app secret — see lessons.md, "Store management — payment and
 * social-login reads return secrets in cleartext". Masking is therefore a courtesy against
 * shoulder-surfing and screen shares, not a security boundary, and the component says so by being
 * honest about what it holds: the value is there, and one click shows it.
 *
 * That is also why it is a real input rather than a "rotate" action. The design's `SecretHint` —
 * last four characters, a rotation date — describes an API that writes secrets and never returns
 * them. This one returns them, and nothing anywhere records when a key was last changed.
 *
 * Binds through `formControlName` like any input; the mask is a `type` swap, so the form never sees
 * anything but the value.
 *
 * ```html
 * <app-secret-field [label]="t('…')" [controlId]="'stripe-secret'">
 *   <input [id]="'stripe-secret'" formControlName="secretKey" />
 * </app-secret-field>
 * ```
 */
@Component({
  selector: 'app-secret-field',
  imports: [Icon],
  template: `
    <span class="control secret" [class.empty]="empty()">
      <app-icon name="lock" />

      <!--
        The projected input. The mask is applied from the host element by the revealed flag, which
        is why the consumer projects a bare input rather than passing a value in: the form owns the
        value, this owns whether it is legible.
      -->
      <ng-content />

      @if (empty()) {
        <span class="not-set">{{ notSetLabel() }}</span>
      }

      <button
        type="button"
        class="text-action"
        [attr.aria-label]="revealLabel()"
        [attr.aria-pressed]="revealed()"
        [disabled]="empty()"
        (click)="revealed.set(!revealed())"
      >
        <app-icon [name]="revealed() ? 'eyeOff' : 'eye'" />
      </button>

      <button
        type="button"
        class="text-action"
        [attr.aria-label]="copyLabel()"
        [disabled]="empty()"
        (click)="copy()"
      >
        <app-icon [name]="copied() ? 'check' : 'copy'" />
      </button>
    </span>
  `,
  styleUrl: './secret-field.css',
  host: {
    // The projected input is a child, so the mask is applied from here rather than bound on it.
    '[class.masked]': '!revealed()',
  },
})
export class SecretField {
  private readonly transloco = inject(TranslocoService);

  /** Names the field in the reveal and copy button labels, so they are distinguishable. */
  readonly label = input.required<string>();
  /** The value, for the copy button and the empty state. The input still owns it in the form. */
  readonly value = input('');
  /** What to say when nothing is stored — which also covers a value written before encryption. */
  readonly notSet = input<string>();

  protected readonly revealed = signal(false);
  protected readonly copied = signal(false);

  protected readonly empty = computed(() => this.value().length === 0);

  protected readonly notSetLabel = computed(
    () => this.notSet() ?? this.transloco.translate('shared.secretField.notSet'),
  );

  protected readonly revealLabel = computed(() =>
    this.transloco.translate(
      this.revealed() ? 'shared.secretField.hide' : 'shared.secretField.reveal',
      {field: this.label()},
    ),
  );

  protected readonly copyLabel = computed(() =>
    this.transloco.translate('shared.secretField.copy', {field: this.label()}),
  );

  /**
   * Puts the value on the clipboard.
   *
   * `navigator.clipboard` is undefined on an insecure origin — which the console is, over plain
   * HTTP in development — so the failure is caught and the tick simply does not appear, rather than
   * an unhandled rejection. Same footgun as `crypto.randomUUID`, found during the orders review.
   */
  protected copy(): void {
    void navigator.clipboard
      ?.writeText(this.value())
      .then(() => {
        this.copied.set(true);
        setTimeout(() => this.copied.set(false), COPIED_MS);
      })
      .catch(() => undefined);
  }
}

/** How long the copy button shows its tick before going back to the clipboard mark. */
const COPIED_MS = 1600;
