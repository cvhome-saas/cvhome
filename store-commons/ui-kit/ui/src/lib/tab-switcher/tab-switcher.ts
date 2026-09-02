import {Component, ElementRef, input, model, viewChildren} from '@angular/core';

import type {Tone} from '../tone';

export interface TabItem {
  readonly key: string;
  readonly label: string;
  /** Outstanding count shown beside the label, e.g. `5`. */
  readonly badge?: string;
  readonly badgeTone?: Tone;
}

/**
 * A pill track for switching between views of the same data.
 *
 * Follows the tablist keyboard contract: one stop in the tab order, arrows move between
 * tabs and select as they go, Home and End jump to the ends. Selection is immediate rather
 * than requiring Enter, which suits filters where every tab is cheap to show.
 *
 * The panel it controls is the consumer's, so pass `panelId` to link them for assistive
 * tech.
 */
@Component({
  selector: 'app-tab-switcher',
  template: `
    <div class="tab-track" role="tablist" [attr.aria-label]="label()">
      @for (tab of tabs(); track tab.key; let index = $index) {
        <button
          #tab
          class="tab"
          type="button"
          role="tab"
          [class.selected]="tab.key === active()"
          [attr.aria-selected]="tab.key === active()"
          [attr.aria-controls]="panelId() || null"
          [tabindex]="tab.key === active() ? 0 : -1"
          (click)="active.set(tab.key)"
          (keydown)="onKeydown($event, index)"
        >
          {{ tab.label }}
          @if (tab.badge) {
            <b class="tab-badge" [class]="tab.badgeTone ?? 'slate'">{{ tab.badge }}</b>
          }
        </button>
      }
    </div>
  `,
  styleUrl: './tab-switcher.css',
})
export class TabSwitcher {
  readonly tabs = input.required<readonly TabItem[]>();
  readonly active = model.required<string>();
  /** Names the group for assistive tech, e.g. "Filter orders by status". */
  readonly label = input.required<string>();
  /** `id` of the region these tabs filter, if the consumer marks one. */
  readonly panelId = input<string | null>(null);

  private readonly buttons = viewChildren<ElementRef<HTMLButtonElement>>('tab');

  protected onKeydown(event: KeyboardEvent, index: number): void {
    const last = this.tabs().length - 1;
    // Arrows follow reading order, so they swap under RTL.
    const forward = getComputedStyle(event.currentTarget as Element).direction === 'rtl' ? -1 : 1;

    const target = ((): number | null => {
      switch (event.key) {
        case 'ArrowRight':
          return index + forward;
        case 'ArrowLeft':
          return index - forward;
        case 'Home':
          return 0;
        case 'End':
          return last;
        default:
          return null;
      }
    })();

    if (target === null) {
      return;
    }

    event.preventDefault();
    // Wrap, so the track has no dead ends.
    const next = ((target % this.tabs().length) + this.tabs().length) % this.tabs().length;
    this.active.set(this.tabs()[next].key);
    this.buttons()[next]?.nativeElement.focus();
  }
}
