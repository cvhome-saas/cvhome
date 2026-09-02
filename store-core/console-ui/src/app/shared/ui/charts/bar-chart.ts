import {Component, computed, inject, input} from '@angular/core';

import {THEME} from '@cvhome-saas/ui-kit/theme';
import {Panel, toneColor, type Tone} from '@cvhome-saas/ui-kit/ui';
import {EChart} from './echart';
import type {EChartsCoreOption} from './echarts-setup';

export interface BarDatum {
  readonly label: string;
  readonly value: number;
  readonly tone: Tone;
}

/**
 * Self-contained categorical breakdown: pass a title and the bars, and it renders the
 * whole panel — plot, value labels, legend, and a total in the header.
 *
 * The total is summed from the data rather than passed in, so the header can never
 * disagree with the chart beneath it.
 *
 * The legend is real DOM rather than an ECharts legend so it stays selectable and styled
 * from the design tokens, and the categories remain readable when the canvas is not.
 */
@Component({
  selector: 'app-bar-chart',
  imports: [EChart, Panel],
  template: `
    <app-panel [title]="title()" [meta]="meta()">
      <div class="bar-body">
        <app-echart [option]="option()" [ariaLabel]="ariaLabel()" />

        @if (showLegend()) {
          <ul class="bar-legend">
            @for (item of data(); track item.label) {
              <li>
                <i class="bar-swatch" [style.background]="color(item.tone)" aria-hidden="true"></i>
                {{ item.label }}
              </li>
            }
          </ul>
        }
      </div>
    </app-panel>
  `,
  styleUrl: './bar-chart.css',
})
export class BarChart {
  private readonly theme = inject(THEME);

  readonly title = input.required<string>();
  readonly data = input.required<readonly BarDatum[]>();
  /**
   * Noun for the summed total shown in the header, e.g. `orders` renders "312 orders".
   * Omit to hide the total.
   */
  readonly unit = input<string | null>(null);
  readonly showLegend = input(true);

  protected readonly total = computed(() =>
    this.data().reduce((sum, item) => sum + item.value, 0),
  );

  protected readonly meta = computed(() => {
    const unit = this.unit();
    return unit ? `${this.total().toLocaleString()} ${unit}` : null;
  });

  protected color(tone: Tone): string {
    return toneColor(this.theme, tone);
  }

  protected readonly ariaLabel = computed(() =>
    this.data()
      .map((item) => `${item.label} ${item.value}`)
      .join(', '),
  );

  protected readonly option = computed<EChartsCoreOption>(() => {
    const theme = this.theme;
    const items = this.data();

    return {
      animationDuration: 800,
      animationEasing: 'cubicOut',
      // Categories live in the DOM legend, so the plot only needs room for the bars and
      // their value labels.
      grid: {top: 24, right: 0, bottom: 0, left: 0, containLabel: false},
      tooltip: {
        trigger: 'item',
        backgroundColor: theme.color('--card'),
        borderColor: theme.color('--border'),
        textStyle: {color: theme.color('--foreground'), fontFamily: theme('--font-sans'), fontSize: 13},
        formatter: '{b}: {c}',
      },
      xAxis: {
        type: 'category',
        data: items.map((item) => item.label),
        axisLine: {show: false},
        axisTick: {show: false},
        axisLabel: {show: false},
      },
      yAxis: {
        type: 'value',
        axisLine: {show: false},
        axisTick: {show: false},
        axisLabel: {show: false},
        splitLine: {show: false},
      },
      series: [
        {
          type: 'bar',
          barMaxWidth: 64,
          itemStyle: {borderRadius: [6, 6, 0, 0]},
          label: {
            show: true,
            position: 'top',
            color: theme.color('--foreground-strong'),
            fontFamily: theme('--font-sans'),
            fontSize: 13,
            fontWeight: 600,
          },
          emphasis: {itemStyle: {opacity: 0.85}},
          data: items.map((item) => ({
            name: item.label,
            value: item.value,
            itemStyle: {color: toneColor(theme, item.tone)},
          })),
        },
      ],
    };
  });
}
