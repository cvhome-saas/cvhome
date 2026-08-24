import {Component, computed, inject, input} from '@angular/core';

import {THEME} from '@core/theme/theme.provider';
import {Panel} from '@shared/ui/panel/panel';
import {EChart} from './echart';
import {toneColor, type Tone} from '../tone';
import type {EChartsCoreOption} from './echarts-setup';

/** One point on the line: a day, a count, and the day as the reader writes it. */
export interface TrendPoint {
  /** The ISO day, used as the category key. */
  readonly key: string;
  /** The day in the reader's language — the axis label and the tooltip's heading. */
  readonly label: string;
  readonly value: number;
}

/**
 * A count per day over a chosen period: pass a title and the points, and it renders the whole panel.
 *
 * **Why a third chart component rather than a bar chart with thirty bars.** `app-bar-chart` is a
 * *categorical* breakdown — it hides its axis labels because the categories live in a DOM legend
 * beside the plot, and prints a value on top of every bar. Thirty days is not a category set: a
 * legend of thirty dates is unreadable, and thirty stacked value labels collide. This is the same
 * vocabulary — the same `Panel`, the same `EChart` host, the same tone-to-token colour, the same
 * tooltip surface — with the mark a time series wants.
 *
 * The total is summed from the data rather than passed in, so the header can never disagree with the
 * plot beneath it, exactly as the bar chart does it.
 *
 * The line's accessible name is the whole series, because a canvas is opaque to assistive tech.
 */
@Component({
  selector: 'app-trend-chart',
  imports: [EChart, Panel],
  template: `
    <app-panel [title]="title()" [meta]="meta()">
      <div class="trend-body">
        <app-echart [option]="option()" [ariaLabel]="ariaLabel()" />
      </div>
    </app-panel>
  `,
  styleUrl: './trend-chart.css',
})
export class TrendChart {
  private readonly theme = inject(THEME);

  readonly title = input.required<string>();
  readonly data = input.required<readonly TrendPoint[]>();
  /** Noun for the summed total shown in the header, e.g. `organizations`. Omit to hide the total. */
  readonly unit = input<string | null>(null);
  readonly tone = input<Tone>('green');
  /** The total already localized. Passed in because number formatting follows the reader's locale. */
  readonly total = input<string | null>(null);

  protected readonly sum = computed(() => this.data().reduce((total, point) => total + point.value, 0));

  protected readonly meta = computed(() => {
    const unit = this.unit();
    if (!unit) {
      return null;
    }
    return `${this.total() ?? this.sum()} ${unit}`;
  });

  protected readonly ariaLabel = computed(() =>
    this.data()
      .map((point) => `${point.label} ${point.value}`)
      .join(', '),
  );

  protected readonly option = computed<EChartsCoreOption>(() => {
    const theme = this.theme;
    const points = this.data();
    const line = toneColor(theme, this.tone());

    return {
      animationDuration: 800,
      animationEasing: 'cubicOut',
      grid: {top: 16, right: 8, bottom: 24, left: 36, containLabel: false},
      tooltip: {
        trigger: 'axis',
        backgroundColor: theme.color('--card'),
        borderColor: theme.color('--border'),
        textStyle: {color: theme.color('--foreground'), fontFamily: theme('--font-sans'), fontSize: 13},
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: points.map((point) => point.label),
        axisLine: {lineStyle: {color: theme.color('--border')}},
        axisTick: {show: false},
        axisLabel: {
          color: theme.color('--muted-foreground'),
          fontFamily: theme('--font-sans'),
          fontSize: 11,
          // A month of dates does not fit; ECharts drops what it cannot draw rather than overlapping.
          hideOverlap: true,
        },
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        axisLine: {show: false},
        axisTick: {show: false},
        axisLabel: {
          color: theme.color('--muted-foreground'),
          fontFamily: theme('--font-sans'),
          fontSize: 11,
        },
        splitLine: {lineStyle: {color: theme.color('--border-soft')}},
      },
      series: [
        {
          type: 'line',
          smooth: 0.3,
          showSymbol: points.length <= 31,
          symbolSize: 5,
          lineStyle: {width: 2, color: line},
          itemStyle: {color: line},
          // The wash under the line is the same hue at low opacity, so it reads as one mark.
          areaStyle: {color: line, opacity: 0.12},
          data: points.map((point) => point.value),
        },
      ],
    };
  });
}
