import {Component, computed, inject, input} from '@angular/core';
import {NbJSThemeVariable, NbThemeService} from '@nebular/theme';
import {toSignal} from '@angular/core/rxjs-interop';
import {EMPTY_STATISTIC_LIST, StatisticList} from '../../services/statistic.api.service';

@Component({
  selector: 'ngx-products-statistic',
  standalone: false,
  template: `
    <div echarts [options]="options()" class="echart"></div>
  `,
})
export class ProductsStatisticComponent {
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
        orient: 'horizontal',
        top: 'top',
        textStyle: {
          color: echarts.textColor,
        },
      },
      xAxis: {},
      yAxis: {
        type: 'category',
        data: data.entries.map(it => it.name)
      },
      series: [
        {
          type: 'bar',
          data: data.entries.map(it => it.value)
        }
      ]
    }
  }
}
