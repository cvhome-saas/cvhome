import {AfterViewInit, Component, EventEmitter, Input, OnDestroy} from '@angular/core';
import {NbJSThemeVariable, NbThemeService} from '@nebular/theme';
import {combineLatest, mergeMap, Observable, Subscription} from "rxjs";
import {map} from "rxjs/operators";
import {StatisticList, StatisticService, StatisticsParams} from "../../service/statistic.service";
import {ErrorService} from '../../../../shared/services/error.service';

@Component({
  selector: 'ngx-products-statistic',
  standalone:false,
  template: `
    <div echarts [options]="options" class="echart"></div>
  `,
})
export class ProductsStatisticComponent implements AfterViewInit, OnDestroy {
  options: any = {};
  @Input()
  paramsEmitter: EventEmitter<StatisticsParams>;
  subscription: Subscription;

  constructor(private theme: NbThemeService, private statisticService: StatisticService, private errorService: ErrorService) {
  }

  getData(p: StatisticsParams): Observable<StatisticList> {
    return this.statisticService.getProductStatistic(p);
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

