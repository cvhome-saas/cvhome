import {Component, input} from '@angular/core';

import {Icon} from '@shared/ui/icon/icon';
import {Panel} from '@shared/ui/panel/panel';

export interface ChecklistItem {
  readonly key: string;
  readonly label: string;
  readonly ok: boolean;
  /** A warning rather than a blocker — translations missing, for instance. */
  readonly soft?: boolean;
}

/** "Before publishing" — the design's sidebar card: a check per requirement, amber when soft. */
@Component({
  selector: 'app-publish-checklist',
  imports: [Icon, Panel],
  template: `
    <app-panel [title]="title()" padded>
      <ul class="checklist">
        @for (item of items(); track item.key) {
          <li
            [class.ok]="item.ok"
            [class.soft]="!item.ok && item.soft"
            [class.missing]="!item.ok && !item.soft"
          >
            <app-icon
              [name]="item.ok ? 'checkCircle' : item.soft ? 'alertCircle' : 'xCircle'"
              [size]="14"
            />
            <span>{{ item.label }}</span>
          </li>
        }
      </ul>
    </app-panel>
  `,
  styles: `
    .checklist {
      list-style: none;
      margin: 0;
      padding: 0;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }
    li {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: var(--text-xs);
      color: var(--foreground);
    }
    li.ok app-icon {
      color: var(--chart-1-foreground);
    }
    li.soft app-icon {
      color: var(--chart-4-foreground);
    }
    li.missing app-icon {
      color: var(--chart-5-foreground);
    }
  `,
})
export class PublishChecklist {
  readonly title = input.required<string>();
  readonly items = input.required<readonly ChecklistItem[]>();
}
