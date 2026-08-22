import {Component, input} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {Icon} from '@shared/ui/icon/icon';
import {IconName} from '@shared/ui/icon/icon-paths';
import type {Tone} from '../tone';

// The shape lives in `@models/ui`: a page's view model is what builds these, and
// `models/orders.ts` was importing it from this component file — a wire-shape module depending on
// a widget's template contract. Re-exported so existing call sites are unaffected.
export type {KpiDatum} from '@models/ui';

/**
 * A single headline metric: label, figure, tone-carrying icon tile, and a footer that is
 * either a movement against the previous period or a status flag.
 *
 * Replaces the former `stat-card`, which emitted class names no stylesheet defined and
 * had no notion of tone or status.
 */
@Component({
  selector: 'app-kpi-card',
  imports: [Icon, TranslocoDirective],
  template: `
    <div class="kpi-head">
      <div class="kpi-copy">
        <p class="kpi-label">{{ label() }}</p>
        <strong class="kpi-value">{{ value() }}</strong>
      </div>
      <span class="kpi-icon" [class]="tone()" aria-hidden="true">
        <app-icon [name]="icon()" />
      </span>
    </div>

    <p class="kpi-foot" *transloco="let t">
      @if (delta(); as movement) {
        <span class="kpi-pill green">
          <app-icon [name]="trend() === 'up' ? 'arrowUp' : 'arrowDown'" />{{ movement }}
        </span>
        <small>{{ t('shared.kpiCard.vsLastPeriod') }}</small>
      } @else if (flag(); as status) {
        <span class="kpi-pill" [class]="tone()">{{ status }}</span>
      }
    </p>
  `,
  styleUrl: './kpi-card.css',
})
export class KpiCard {
  readonly label = input.required<string>();
  readonly value = input.required<string>();
  readonly icon = input.required<IconName>();
  readonly tone = input<Tone>('slate');
  /** Movement against the comparison period, e.g. `12.4%`. */
  readonly delta = input<string | null>(null);
  readonly trend = input<'up' | 'down'>('up');
  /** Shown instead of a delta when the metric reports a state rather than a change. */
  readonly flag = input<string | null>(null);
}
