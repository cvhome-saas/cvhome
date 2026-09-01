import {Component, input} from '@angular/core';

import {KpiCard, type KpiDatum} from '../kpi-card/kpi-card';

/**
 * A labelled row of metrics: pass a label and the data, and it lays the cards out and
 * staggers their entrance.
 *
 * Columns collapse on the grid's own width rather than the viewport's, so the row behaves
 * correctly wherever it is placed — full width, in a split layout, or inside a drawer.
 */
@Component({
  selector: 'app-kpi-grid',
  imports: [KpiCard],
  template: `
    <section class="kpi-grid" [attr.aria-label]="label()" [style.--kpi-columns]="columns()">
      @for (kpi of data(); track kpi.label) {
        <app-kpi-card
          [label]="kpi.label"
          [value]="kpi.value"
          [icon]="kpi.icon"
          [tone]="kpi.tone ?? 'slate'"
          [delta]="kpi.delta ?? null"
          [trend]="kpi.trend ?? 'up'"
          [flag]="kpi.flag ?? null"
        />
      }
    </section>
  `,
  styleUrl: './kpi-grid.css',
})
export class KpiGrid {
  readonly data = input.required<readonly KpiDatum[]>();
  /** Names the group for assistive tech, e.g. "Store metrics". */
  readonly label = input.required<string>();
  /** Columns at full width; narrower containers step down to two and then one. */
  readonly columns = input(4);
}
