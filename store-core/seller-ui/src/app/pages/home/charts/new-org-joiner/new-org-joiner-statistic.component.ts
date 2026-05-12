import {AfterViewInit, Component, EventEmitter, Input, OnDestroy} from '@angular/core';
import {NbJSThemeVariable, NbThemeService} from '@nebular/theme';
import {combineLatest, mergeMap, Observable, Subscription} from "rxjs";
import {map} from "rxjs/operators";
import {StatisticList, StatisticService, StatisticsParams} from "../../service/statistic.service";
import {ErrorService} from '../../../shared/services/error.service';

@Component({
  selector: 'ngx-new-org-joiner-statistic',
  standalone: false,
  template: `
    <div echarts [options]="options" class="echart"></div>
  `,
})
export class NewOrgJoinerStatisticComponent implements AfterViewInit, OnDestroy {
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
    return this.statisticService.getNewOrgJoinerStatistic(p)
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
