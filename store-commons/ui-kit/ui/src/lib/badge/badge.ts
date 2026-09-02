import {Component, input} from '@angular/core';

import type {Tone} from '../tone';

export type BadgeTone = Tone;

/** Categorical wash + matching on-dark ink. Colour is never the only signal — the badge
 *  always carries a label. */
const TONE_CLASSES: Record<BadgeTone, string> = {
  green: 'bg-chart-1-wash text-chart-1-foreground ring-chart-1/25',
  blue: 'bg-chart-2-wash text-chart-2-foreground ring-chart-2/25',
  cyan: 'bg-chart-3-wash text-chart-3-foreground ring-chart-3/25',
  amber: 'bg-chart-4-wash text-chart-4-foreground ring-chart-4/25',
  red: 'bg-chart-5-wash text-chart-5-foreground ring-chart-5/25',
  violet: 'bg-chart-6-wash text-chart-6-foreground ring-chart-6/25',
  slate: 'bg-input text-muted-foreground ring-border',
};

/** A count reads as a pill; a status reads as a plate. */
export type BadgeShape = 'pill' | 'square';

@Component({
  selector: 'app-badge',
  template: `<ng-content />`,
  host: {'[class]': 'classes()'},
})
export class Badge {
  readonly tone = input<BadgeTone>('slate');
  readonly shape = input<BadgeShape>('pill');

  protected classes(): string {
    const radius = this.shape() === 'pill' ? 'rounded-full' : 'rounded-sm';
    return `inline-flex items-center gap-1 ${radius} px-2 py-0.5 text-operations-label uppercase ring-1 ${TONE_CLASSES[this.tone()]}`;
  }
}
