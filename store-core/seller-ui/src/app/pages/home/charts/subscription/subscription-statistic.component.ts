import {Component, computed, inject, input} from '@angular/core';
import {NbJSThemeVariable, NbThemeService} from '@nebular/theme';
import {toSignal} from '@angular/core/rxjs-interop';
import {NgxEchartsDirective} from 'ngx-echarts';
import {EMPTY_STATISTIC_LIST, StatisticList} from '../../services/statistic.api.service';

@Component({
  selector: 'ngx-subscription-statistic',
  standalone: true,
  imports: [NgxEchartsDirective],
  template: `
    <div echarts [options]="options()" class="echart"></div>
  `,
})
export class SubscriptionStatisticComponent {
  private readonly theme = inject(NbThemeService);
  private readonly themeConfig = toSignal(this.theme.getJsTheme());

  readonly data = input<StatisticList>(EMPTY_STATISTIC_LIST);

  readonly options = computed(() => {
    const config = this.themeConfig();
    return config ? this.buildOptions(config.variables, this.data()) : {};
  });

  private buildOptions(colors: NbJSThemeVariable, data: StatisticList): any {
    const echarts: any = colors.echarts;

    return {
      backgroundColor: echarts.bg,
      legend: {
        orient: 'vertical',
        left: 'left',
        textStyle: {
          color: echarts.textColor,
        },
      },
      series: [
        {
          name: 'Countries',
          type: 'pie',
          radius: '80%',
          center: ['50%', '50%'],
          data: data.entries,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: echarts.itemHoverShadowColor,
            },
          }
        },
      ],
    };
  }
}
