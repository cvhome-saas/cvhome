import {Component, input} from '@angular/core';

import {Icon} from '@shared/ui/icon/icon';
import {IconName} from '@shared/ui/icon/icon-paths';
import type {Tone} from '../tone';

/**
 * A full-width inline notice: an icon, a line of copy, and whatever the reader can do
 * about it.
 *
 * Sits inside the surface it concerns — above a table's rows, under a panel header —
 * rather than floating like a toast, because it describes what is on screen rather than
 * the outcome of an action.
 *
 * Project actions as children:
 *
 * ```html
 * <app-notice-bar tone="amber" icon="alertCircle" message="5 orders are overdue.">
 *   <button type="button">Mark as processed</button>
 * </app-notice-bar>
 * ```
 */
@Component({
  selector: 'app-notice-bar',
  imports: [Icon],
  template: `
    <app-icon [name]="icon()" />
    <p class="notice-copy">{{ message() }}</p>
    <ng-content />
  `,
  styleUrl: './notice-bar.css',
  host: {
    '[class]': 'tone()',
    role: 'status',
  },
})
export class NoticeBar {
  readonly message = input.required<string>();
  readonly icon = input.required<IconName>();
  readonly tone = input<Tone>('amber');
}
