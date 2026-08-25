import {Component, computed, input} from '@angular/core';

import {Panel} from '@shared/ui/panel/panel';

export interface RankedItem {
  readonly label: string;
  readonly value: number;
}

/**
 * Self-contained leaderboard: pass a title and the items, and it renders the whole panel —
 * rank chips, labels, proportional bars and values, ordered highest first.
 *
 * Bar widths are derived from the largest value, so callers pass measurements rather than
 * pre-computed percentages that could drift out of step with them.
 *
 * Project a header action with the `panelAction` attribute, as with `app-panel`.
 */
@Component({
  selector: 'app-ranked-list',
  imports: [Panel],
  template: `
    <app-panel [title]="title()" [meta]="meta()">
      <ng-content select="[panelAction]" ngProjectAs="[panelAction]" />

      <ol class="ranked-list">
        @for (row of rows(); track row.label) {
          <li>
            <b class="ranked-rank" [class.lead]="row.rank === 1">{{ row.rank }}</b>
            <div class="ranked-copy">
              <span class="ranked-label">{{ row.label }}</span>
              <span class="ranked-track">
                <i [style.width.%]="row.share"></i>
              </span>
            </div>
            <small class="ranked-value">{{ row.display }}</small>
          </li>
        }
      </ol>
    </app-panel>
  `,
  styleUrl: './ranked-list.css',
})
export class RankedList {
  readonly title = input.required<string>();
  readonly data = input.required<readonly RankedItem[]>();
  readonly meta = input<string | null>(null);

  protected readonly rows = computed(() => {
    const items = [...this.data()].sort((a, b) => b.value - a.value);
    const top = items[0]?.value || 1;

    return items.map((item, index) => ({
      label: item.label,
      rank: index + 1,
      share: Math.round((item.value / top) * 100),
      display: item.value.toLocaleString(),
    }));
  });
}
