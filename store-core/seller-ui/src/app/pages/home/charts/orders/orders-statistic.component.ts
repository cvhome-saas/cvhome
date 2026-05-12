import {AfterViewInit, Component, EventEmitter, Input, OnDestroy} from '@angular/core';
import {NbJSThemeVariable, NbThemeService} from '@nebular/theme';
import {combineLatest, mergeMap, Observable, of, Subscription} from "rxjs";
import {map} from "rxjs/operators";
import {StatisticList, StatisticService, StatisticsParams} from "../../service/statistic.service";
import {ErrorService} from '../../../shared/services/error.service';

@Component({
  selector: 'ngx-orders-statistic',
  standalone: false,
  template: `
    <div echarts [options]="options" class="echart"></div>
  `,
})
export class OrdersStatisticComponent implements AfterViewInit, OnDestroy {
  options: any = {};
  @Input()
  paramsEmitter: EventEmitter<StatisticsParams>;
  subscription: Subscription;

  constructor(private theme: NbThemeService, private statisticService: StatisticService, private errorService: ErrorService) {
  }

  getData(p: StatisticsParams): Observable<StatisticList> {
    if (p.store) {
      return this.statisticService.getOrderStatistic(p);
    } else {
      return of({
        entries: []
      })
    }
  }

  ngAfterViewInit() {
    this.subscription = combineLatest([this.paramsEmitter, this.theme.getJsTheme()])
      .pipe(mergeMap(([params, config]) => {
        return this.getData(params).pipe(map((data: StatisticList) => {
          return {
            config: config.variables,
            data: data
          }
        }))
      }))
      .subscribe((result) => {
        this.options = this.buildOptions(result.config, result.data);
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });

  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  buildOptions(colors: NbJSThemeVariable, data: StatisticList): any {
    let echarts: any = colors.echarts;

    const status = [...new Set(data.entries.map(it => it.name))];

    let groupByDate = data.entries.reduce(
      (result, item) => ({
        ...result,
        [item.date]: [
          ...(result[item.date] || []),
          item,
        ],
      }),
      {},
    );

    let source = Object.keys(groupByDate).map(it => {
      let dateSeries = {
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
      series: status.map(it => {
        return {type: "bar"}
      })
    }
  }
}
