import {AfterViewInit, Component, EventEmitter, Input, OnDestroy} from '@angular/core';
import {NbJSThemeVariable, NbThemeService} from '@nebular/theme';
import {combineLatest, mergeMap, Observable, Subscription} from "rxjs";
import {map} from "rxjs/operators";
import {StatisticList, StatisticService, StatisticsParams} from "../../service/statistic.service";
import {ErrorService} from '../../../../shared/services/error.service';

@Component({
  selector: 'ngx-subscription-statistic',
  standalone:false,
  template: `
    <div echarts [options]="options" class="echart"></div>
  `,
})
export class SubscriptionStatisticComponent implements AfterViewInit, OnDestroy {
  options: any = {};
  @Input()
  paramsEmitter: EventEmitter<StatisticsParams>;
  subscription: Subscription;

  constructor(private theme: NbThemeService,
              private statisticService: StatisticService,
              private errorService: ErrorService
  ) {
  }

  getData(p: StatisticsParams): Observable<StatisticList> {
    return this.statisticService.getSubscriptionStatistic(p)
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
