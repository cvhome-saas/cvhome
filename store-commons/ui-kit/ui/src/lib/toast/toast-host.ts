import {Component, inject} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {Icon} from '../icon/icon';
import {IconName} from '../icon/icon-paths';
import {ToastService, type ToastTone} from './toast';

const TONE_ICON: Record<ToastTone, IconName> = {
  success: 'checkCircle',
  info: 'info',
  warning: 'alertCircle',
  danger: 'xCircle',
};

/**
 * Renders every live `ToastService` message as an animated, dismissible card.
 *
 * Mounted once at the app root (`app.html`), so it floats above whatever page is showing
 * rather than belonging to any one layout.
 */
@Component({
  selector: 'app-toast-host',
  imports: [Icon, TranslocoDirective],
  template: `
    <div
      class="toast-stack"
      role="region"
      aria-live="polite"
      aria-relevant="additions"
      [attr.aria-label]="t('shared.toast.regionLabel')"
      *transloco="let t"
    >
      @for (message of toasts.messages(); track message.id) {
        <div
          class="toast"
          [class]="message.tone"
          role="status"
          animate.enter="toast-in"
          animate.leave="toast-out"
          (mouseenter)="toasts.pause(message.id)"
          (mouseleave)="toasts.resume(message.id)"
        >
          <span class="toast-icon"><app-icon [name]="icon(message.tone)" /></span>
          <p class="toast-text">
            <span class="sr-only">{{ t('shared.toast.tone.' + message.tone) }}</span>
            {{ message.text }}
          </p>
          <button
            class="toast-close"
            type="button"
            [attr.aria-label]="t('shared.toast.dismiss')"
            (click)="toasts.dismiss(message.id)"
          >
            <app-icon name="x" />
          </button>
          @if (message.durationMs) {
            <span class="toast-progress" [style.animation-duration.ms]="message.durationMs"></span>
          }
        </div>
      }
    </div>
  `,
  styleUrl: './toast-host.css',
})
export class ToastHost {
  protected readonly toasts = inject(ToastService);

  protected icon(tone: ToastTone): IconName {
    return TONE_ICON[tone];
  }
}
