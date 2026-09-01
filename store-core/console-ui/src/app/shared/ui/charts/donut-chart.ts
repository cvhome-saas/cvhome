import {Component, computed, inject, input} from '@angular/core';

import {THEME} from '@cvhome-saas/ui-kit/theme';
import {Panel, toneColor, type Tone} from '@cvhome-saas/ui-kit/ui';
import {EChart} from './echart';
import type {EChartsCoreOption} from './echarts-setup';

export interface DonutSlice {
  readonly label: string;
  readonly value: number;
  readonly tone: Tone;
}

/**
 * Self-contained share-of-total widget: pass a title and the slices, and it renders the
 * whole panel — ring, legend and percentages.
 *
 * Percentages are derived from the values, so callers never compute a complement or hand
 * in a second copy of the same numbers.
 */
@Component({
  selector: 'app-donut-chart',
  imports: [EChart, Panel],
  template: `
    <app-panel [title]="title()">
      <div class="donut-body">
        <app-echart class="donut-ring" [option]="option()" [ariaLabel]="ariaLabel()" />

        <dl class="donut-legend">
          @for (slice of slices(); track slice.label) {
            <div>
              <dt>
                <i class="donut-swatch" [style.background]="slice.color" aria-hidden="true"></i>
                {{ slice.label }}
              </dt>
              <dd>{{ slice.percent }}%</dd>
            </div>
          }
        </dl>
      </div>
    </app-panel>
  `,
  styleUrl: './donut-chart.css',
})
export class DonutChart {
  private readonly theme = inject(THEME);

  readonly title = input.required<string>();
  readonly data = input.required<readonly DonutSlice[]>();
  /** Ring thickness as an inner/outer radius pair. */
  readonly thickness = input<[string, string]>(['66%', '92%']);

  private readonly total = computed(
    () => this.data().reduce((sum, slice) => sum + slice.value, 0) || 1,
  );

  /** Slices decorated with everything the template needs, so it stays declarative. */
  protected readonly slices = computed(() =>
    this.data().map((slice) => ({
      label: slice.label,
      percent: Math.round((slice.value / this.total()) * 100),
      color: toneColor(this.theme, slice.tone),
    })),
  );

  protected readonly ariaLabel = computed(() =>
    this.slices()
      .map((slice) => `${slice.label} ${slice.percent}%`)
      .join(', '),
  );

  protected readonly option = computed<EChartsCoreOption>(() => {
    const theme = this.theme;

    return {
      animationDuration: 700,
      animationEasing: 'cubicOut',
      tooltip: {
        trigger: 'item',
        backgroundColor: theme.color('--card'),
        borderColor: theme.color('--border'),
        textStyle: {color: theme.color('--foreground'), fontFamily: theme('--font-sans'), fontSize: 13},
        formatter: '{b}: {d}%',
      },
      series: [
        {
          type: 'pie',
          radius: this.thickness(),
          center: ['50%', '50%'],
          avoidLabelOverlap: false,
          label: {show: false},
          labelLine: {show: false},
          // One continuous band; borders would read as separate arcs.
          itemStyle: {borderWidth: 0},
          emphasis: {scale: false, itemStyle: {opacity: 0.85}},
          data: this.data().map((slice) => ({
            name: slice.label,
            value: slice.value,
            itemStyle: {color: toneColor(theme, slice.tone)},
          })),
        },
      ],
    };
  });
}
