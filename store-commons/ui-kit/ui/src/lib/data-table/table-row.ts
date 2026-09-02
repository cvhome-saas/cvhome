import {Component, input} from '@angular/core';

/**
 * One record in an `app-data-table`.
 *
 * Takes its column geometry from the table's `--table-columns`, so a row never restates the
 * track list and the two cannot drift apart.
 *
 * Rows are frequently interactive. Rather than wrap a link or button — which would put a
 * focusable box around every cell — set `interactive` and host the row on whatever element
 * the consumer needs via `attr.role`/`tabindex`, or nest a single control in one cell.
 */
@Component({
  selector: 'app-table-row',
  template: `<ng-content />`,
  styleUrl: './table-row.css',
  host: {
    role: 'row',
    '[class.interactive]': 'interactive()',
  },
})
export class TableRow {
  /** Adds the hover affordance for rows that lead somewhere. */
  readonly interactive = input(false);
}
