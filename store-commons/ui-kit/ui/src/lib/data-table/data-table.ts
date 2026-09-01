import {Component, computed, input} from '@angular/core';

/** One column's header text and its share of the row's width. */
export interface TableColumn {
  readonly key: string;
  /** Header text. Empty renders an unlabelled track — selection boxes, row actions. */
  readonly label: string;
  /** A CSS grid track: `34px`, `1.5fr`, `minmax(0, 1fr)`. */
  readonly width: string;
  readonly align?: 'start' | 'end';
}

/**
 * The surface, column geometry and header row of a record list.
 *
 * It deliberately does not render cells. Rows carry links, badges, avatars and per-row
 * actions that no generic cell renderer would express well, so the consumer writes them
 * with `app-table-row` and this owns everything around them:
 *
 * ```html
 * <app-data-table [columns]="columns" label="All orders">
 *   @for (order of orders(); track order.id) {
 *     <app-table-row> … cells … </app-table-row>
 *   }
 * </app-data-table>
 * ```
 *
 * Header and rows stay aligned because the track list is published as `--table-columns`,
 * which rows inherit — they are projected content, so no selector could reach them.
 *
 * Below its narrow breakpoint the grid stops applying and rows restack; the consumer's
 * cells supply their own labels there, since only they know what each cell means.
 */
@Component({
  selector: 'app-data-table',
  template: `
    <div class="table-scroll">
      <div class="table-grid" role="table" [attr.aria-label]="label() || null">
        <div class="table-head" role="row">
          @for (column of columns(); track column.key) {
            <span
              class="table-heading"
              role="columnheader"
              [class.end]="column.align === 'end'"
              >{{ column.label }}</span
            >
          }
        </div>

        <ng-content />
      </div>
    </div>
  `,
  styleUrl: './data-table.css',
  host: {
    '[style.--table-columns]': 'tracks()',
  },
})
export class DataTable {
  readonly columns = input.required<readonly TableColumn[]>();
  /** Names the table for assistive tech, e.g. "All orders". */
  readonly label = input('');

  protected readonly tracks = computed(() =>
    this.columns()
      .map((column) => column.width)
      .join(' '),
  );
}
