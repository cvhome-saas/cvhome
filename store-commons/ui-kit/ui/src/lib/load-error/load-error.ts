import {Component, inject, input, output} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import {NoticeBar} from '../notice-bar/notice-bar';

/**
 * A page or panel could not load, and the one thing to do about it.
 *
 * Nine features drew this by hand, in four visually distinct shapes: two radii, two background
 * tokens, three text colours, and four different retry buttons — a `secondary-action` in three
 * places, a red outline in orders, a neutral card chip in billing, and three more bespoke
 * `.load-error button` rules. All nine used the same copy, and all nine took it from
 * `dashboard.tryAgain`, a key in a namespace eight of them had nothing to do with. That key now
 * lives at `shared.actions.retry` and this component owns the rendering.
 *
 * Built on `app-notice-bar` rather than beside it: a notice bar is already "something to say about
 * what is on screen, inside the surface it concerns, with an action projected", which is exactly
 * this. The tone is fixed to red because a load failure has no other tone.
 *
 * ```html
 * @if (facade.error()) {
 *   <app-load-error [message]="t('orders.loadFailed')" (retry)="facade.retry()" />
 * }
 * ```
 */
@Component({
  selector: 'app-load-error',
  imports: [NoticeBar],
  template: `
    <app-notice-bar tone="red" icon="alertCircle" role="alert" [message]="message()">
      <button class="secondary-action" type="button" (click)="retry.emit()">
        {{ retryLabel() }}
      </button>
    </app-notice-bar>
  `,
  styles: `
    :host {
      display: block;
    }
  `,
})
export class LoadError {
  private readonly transloco = inject(TranslocoService);

  readonly message = input.required<string>();
  /** Overrides the shared "Try again", for the rare case where retrying means something specific. */
  readonly label = input<string | null>(null);

  readonly retry = output<void>();

  protected retryLabel(): string {
    return this.label() ?? this.transloco.translate('shared.actions.retry');
  }
}
