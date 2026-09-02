import {Component, input} from '@angular/core';

/**
 * Veils its content while a request is in flight.
 *
 * Wraps rather than replaces: the previous data stays on screen, dimmed, so the layout
 * does not collapse and the reader keeps their place. The content is marked `inert` while
 * busy, so it cannot be clicked or tabbed into behind the veil.
 *
 * **`reserve` is for the first load**, when there is nothing to veil yet. Eight pages solved that
 * by declaring their own `.first-load` placeholder slab, at three different heights — 26rem, 24rem
 * and 60vh — so the amount the page jumped when data arrived depended on which page you were on.
 * Passing `reserve` gives the overlay a minimum height while `busy` and the content is empty, and
 * nothing to do otherwise. `page` is for a whole route, `panel` for a widget inside one.
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
    '[class.reserve-panel]': "reserve() === 'panel'",
    '[class.reserve-page]': "reserve() === 'page'",
  },
})
export class BusyOverlay {
  readonly busy = input.required<boolean>();
  readonly label = input('Loading…');
  /**
   * How much room to hold open while the first load runs and there is nothing to show.
   * `none` for a region that already has a size of its own.
   */
  readonly reserve = input<'none' | 'panel' | 'page'>('none');
}
