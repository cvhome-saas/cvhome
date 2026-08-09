import {Component, computed, inject, input} from '@angular/core';
import {NbJSThemeVariable, NbThemeService} from '@nebular/theme';
import {toSignal} from '@angular/core/rxjs-interop';
import {NgxEchartsDirective} from 'ngx-echarts';
import {EMPTY_STATISTIC_LIST, StatisticList} from 'seller-core/analytics';
import {EChartsLikeOption, NbEchartsTheme} from '../../models/echarts.model';

@Component({
  selector: 'ngx-new-org-joiner-statistic',
  standalone: true,
  imports: [NgxEchartsDirective],
  template: `
    <div echarts [options]="options()" class="echart"></div>
  `,
})
export class NewOrgJoinerStatisticComponent {
  private readonly theme = inject(NbThemeService);
  private readonly themeConfig = toSignal(this.theme.getJsTheme());

  readonly data = input<StatisticList>(EMPTY_STATISTIC_LIST);

  readonly options = computed(() => {
    const config = this.themeConfig();
    return config ? this.buildOptions(config.variables, this.data()) : {};
  });

  private buildOptions(colors: NbJSThemeVariable, data: StatisticList): EChartsLikeOption {
    const echarts = colors.echarts as unknown as NbEchartsTheme;

    return {
      backgroundColor: echarts.bg,
      legend: {
        orient: 'vertical',
        left: 'left',
        textStyle: {
          color: echarts.textColor,
        },
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: [
        {
          type: 'category',
          data: data.entries.map(it => it.date),
          axisTick: {
            alignWithLabel: true
          }
        }
      ],
      yAxis: [
        {
          type: 'value'
        }
      ],
      series: [
        {
          type: 'bar',
          barWidth: '60%',
          data: data.entries.map(it => it.value),
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: echarts.itemHoverShadowColor,
            },
          }
        }
      ]
    }
  }
}
