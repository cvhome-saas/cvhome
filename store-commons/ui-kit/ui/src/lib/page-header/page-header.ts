import {Component, input} from '@angular/core';

/**
 * The title block a console page opens with: heading, supporting context, and whatever
 * actions the page needs.
 *
 * Lives with the page rather than the shell, because the title, the context line and the
 * actions are the page's own. Project actions as children:
 *
 * ```html
 * <app-page-header title="Home" context="Acme Supply Co.">
 *   <button>Export</button>
 * </app-page-header>
 * ```
 */
@Component({
  selector: 'app-page-header',
  template: `
    <div class="page-head">
      <div class="page-copy">
        <h1>{{ title() }}</h1>
        @if (context()) {
          <p>{{ context() }}</p>
        }
      </div>
      <ng-content />
    </div>
  `,
  styleUrl: './page-header.css',
})
export class PageHeader {
  readonly title = input.required<string>();
  readonly context = input<string | null>(null);
}
