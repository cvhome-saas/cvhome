import {Component, input} from '@angular/core';

/**
 * The card chrome every dashboard widget sits in: a titled header with optional meta and
 * actions, over a body.
 *
 * Widgets compose this rather than repeating the card border, radius and header rules, so
 * there is one definition of what a panel looks like.
 *
 * Project an action into the header with the `panelAction` attribute:
 *
 * ```html
 * <app-panel title="Top selling products">
 *   <button panelAction type="button">…</button>
 * </app-panel>
 * ```
 */
@Component({
  selector: 'app-panel',
  template: `
    <header class="panel-head">
      <div class="panel-copy">
        <h2 class="panel-title">{{ title() }}</h2>
        @if (subtitle()) {
          <p class="panel-subtitle">{{ subtitle() }}</p>
        }
      </div>
      @if (meta()) {
        <span class="panel-meta">{{ meta() }}</span>
      }
      <ng-content select="[panelAction]" />
    </header>

    <div class="panel-body">
      <ng-content />
    </div>
  `,
  styleUrl: './panel.css',
})
export class Panel {
  readonly title = input.required<string>();
  /** Secondary figure shown beside the title, e.g. a total. */
  readonly meta = input<string | null>(null);
  /** A line under the title saying what the panel is showing. */
  readonly subtitle = input<string | null>(null);
}
