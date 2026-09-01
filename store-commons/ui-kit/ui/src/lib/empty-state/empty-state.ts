import {Component, booleanAttribute, input} from '@angular/core';

import {Icon} from '../icon/icon';
import {IconName} from '../icon/icon-paths';

/**
 * Nothing to show, and why.
 *
 * Nine class names said this before it existed — `.table-empty` (drawn two different ways in orders
 * and products), `.list-empty`, `.editor-empty`, `.picker-empty`, `.member-empty`, `.empty-note`,
 * `.related-empty`, `.plain .empty` — so a merchant with no products and a merchant with no orders
 * met two different designs on consecutive pages.
 *
 * The shape is the orders one, which was the fuller of the two: an icon in a medallion, a line of
 * copy capped at a readable measure, and whatever the reader can do about it projected underneath.
 *
 * **The distinction worth keeping is "nothing yet" versus "nothing matched".** The first is a
 * state the operator cannot act on and should not be offered a button for; the second is a filter
 * they can clear. Both are this component — the difference is whether an action is projected.
 *
 * ```html
 * <app-empty-state icon="search" [message]="t('products.noneMatch')">
 *   <button class="secondary-action" type="button" (click)="clear()">{{ t('products.clear') }}</button>
 * </app-empty-state>
 * ```
 */
@Component({
  selector: 'app-empty-state',
  imports: [Icon],
  template: `
    <app-icon [name]="icon()" />
    @if (title()) {
      <h3 class="empty-title">{{ title() }}</h3>
    }
    <p class="empty-copy">{{ message() }}</p>
    <ng-content />
  `,
  styleUrl: './empty-state.css',
  host: {role: 'status', '[class.compact]': 'compact()'},
})
export class EmptyState {
  readonly message = input.required<string>();
  readonly icon = input<IconName>('info');
  /** A heading above the message, for an empty state that fills a whole panel rather than a table. */
  readonly title = input<string | null>(null);
  /** Tightens the padding for an empty state inside a narrow column rather than a full table. */
  readonly compact = input(false, {transform: booleanAttribute});
}
