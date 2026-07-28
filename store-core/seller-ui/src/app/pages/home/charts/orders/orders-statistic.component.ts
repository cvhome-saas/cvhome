import {Component, computed, inject, input} from '@angular/core';
import {NbJSThemeVariable, NbThemeService} from '@nebular/theme';
import {toSignal} from '@angular/core/rxjs-interop';
import {EMPTY_STATISTIC_LIST, StatisticList} from '../../services/statistic.api.service';

@Component({
  selector: 'ngx-orders-statistic',
  standalone: false,
  template: `
    <div echarts [options]="options()" class="echart"></div>
  `,
})
export class OrdersStatisticComponent {
  private readonly theme = inject(NbThemeService);
  private readonly themeConfig = toSignal(this.theme.getJsTheme());

  readonly data = input<StatisticList>(EMPTY_STATISTIC_LIST);

  readonly options = computed(() => {
    const config = this.themeConfig();
    return config ? this.buildOptions(config.variables, this.data()) : {};
  });

  private buildOptions(colors: NbJSThemeVariable, data: StatisticList): any {
    const echarts: any = colors.echarts;

    const status = [...new Set(data.entries.map(it => it.name))];

    const groupByDate = data.entries.reduce(
      (result, item) => ({
        ...result,
        [item.date]: [
          ...(result[item.date] || []),
          item,
        ],
      }),
      {},
    );

    const source = Object.keys(groupByDate).map(it => {
      const dateSeries = {
        product: it,
      };
      groupByDate[it].forEach(dateValues => {
        dateSeries[dateValues.name] = dateValues.value
      });
      return dateSeries;
    })

    return {
      backgroundColor: echarts.bg,
      legend: {
        orient: 'horizontal',
        top: 'top',
        textStyle: {
          color: echarts.textColor,
        },
      },
      dataset: {
        dimensions: ['product', ...status],
        source: source
      },
      xAxis: {type: 'category'},
      yAxis: {},
      series: status.map(() => {
        return {type: "bar"}
      })
    }
  }
}
