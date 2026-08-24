import {Component, input, output} from '@angular/core';

import {Icon} from '@shared/ui/icon/icon';
import {IconName} from '@shared/ui/icon/icon-paths';
import {Panel} from '@shared/ui/panel/panel';
import type {Tone} from '../tone';

export interface ActionItem {
  readonly label: string;
  readonly detail: string;
  /** Outstanding count shown as a chip, e.g. `7`. */
  readonly count: string;
  readonly icon: IconName;
  readonly tone: Tone;
}

/**
 * Self-contained queue of things needing attention: pass a title and the items, and it
 * renders the whole panel — icon tiles, copy, counts and row affordances.
 *
 * Each row is a button that emits `itemSelect`, so the consumer decides where a row leads
 * without owning any of the markup.
 *
 * Project a header action with the `panelAction` attribute, as with `app-panel`.
 */
@Component({
  selector: 'app-action-list',
  imports: [Icon, Panel],
  template: `
    <app-panel [title]="title()" [meta]="meta()">
      <ng-content select="[panelAction]" ngProjectAs="[panelAction]" />

      <ul class="action-list">
        @for (item of data(); track item.label) {
          <li>
            <button type="button" (click)="itemSelect.emit(item)">
              <span class="action-icon" [class]="item.tone" aria-hidden="true">
                <app-icon [name]="item.icon" />
              </span>
              <span class="action-copy">
                <strong>{{ item.label }}</strong>
                <small>{{ item.detail }}</small>
              </span>
              <span class="action-count" [class]="item.tone">{{ item.count }}</span>
              <app-icon class="action-chevron" name="chevronRight" [flip]="true" />
            </button>
          </li>
        }
      </ul>
    </app-panel>
  `,
  styleUrl: './action-list.css',
})
export class ActionList {
  readonly title = input.required<string>();
  readonly data = input.required<readonly ActionItem[]>();
  readonly meta = input<string | null>(null);

  /** Named `itemSelect` rather than `select` to avoid shadowing the native DOM event. */
  readonly itemSelect = output<ActionItem>();
}
