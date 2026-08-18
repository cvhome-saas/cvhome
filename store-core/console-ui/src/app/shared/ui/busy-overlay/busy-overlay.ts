import {Component, input} from '@angular/core';

/**
 * Veils its content while a request is in flight.
 *
 * Wraps rather than replaces: the previous data stays on screen, dimmed, so the layout
 * does not collapse and the reader keeps their place. The content is marked `inert` while
 * busy, so it cannot be clicked or tabbed into behind the veil.
 */
@Component({
  selector: 'app-busy-overlay',
  template: `
    <div class="busy-content" [attr.inert]="busy() ? '' : null">
      <ng-content />
    </div>

    @if (busy()) {
      <div class="busy-veil" animate.enter="busy-in" animate.leave="busy-out">
        <div class="busy-badge">
          <span class="busy-spinner" aria-hidden="true"></span>
          <span class="busy-label">{{ label() }}</span>
        </div>
      </div>
    }
  `,
  styleUrl: './busy-overlay.css',
  host: {
    '[attr.aria-busy]': 'busy()',
  },
})
export class BusyOverlay {
  readonly busy = input.required<boolean>();
  readonly label = input('Loading…');
}
