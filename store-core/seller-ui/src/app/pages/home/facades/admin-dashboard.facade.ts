import {Injectable, inject, signal} from '@angular/core';
import {forkJoin} from 'rxjs';
import {DateRangeStateService} from '../state/date-range.state';
import {ApiErrorService} from '../../../core/errors/api-error.service';
import {EMPTY_STATISTIC_LIST, StatisticApiService, StatisticList, StatisticsParams} from '../services/statistic.api.service';

@Injectable()
export class AdminDashboardFacade {
  private readonly dateRangeState = inject(DateRangeStateService);
  private readonly statisticApi = inject(StatisticApiService);
  private readonly apiErrors = inject(ApiErrorService);

  readonly fromDate = this.dateRangeState.fromDate;
  readonly toDate = this.dateRangeState.toDate;
  readonly fromMaxDate = new Date();
  readonly toMaxDate = new Date();

  readonly loading = signal<boolean>(false);
  readonly subscriptionData = signal<StatisticList>(EMPTY_STATISTIC_LIST);
  readonly newOrgJoinerData = signal<StatisticList>(EMPTY_STATISTIC_LIST);
  readonly newStoreCreatedData = signal<StatisticList>(EMPTY_STATISTIC_LIST);

  init(): void {
    this.loadAll();
  }

  onFromDateChange(date: Date): void {
    this.dateRangeState.setFromDate(date);
    this.loadAll();
  }

  onToDateChange(date: Date): void {
    this.dateRangeState.setToDate(date);
    this.loadAll();
  }

  private loadAll(): void {
    const params = this.buildParams();
    this.loading.set(true);

    forkJoin({
      subscription: this.statisticApi.getSubscriptionStatistic(params),
      newOrgJoiner: this.statisticApi.getNewOrgJoinerStatistic(params),
      newStoreCreated: this.statisticApi.getNewStoreCreatedStatistic(params),
    }).subscribe({
      next: (result) => {
        this.subscriptionData.set(result.subscription);
        this.newOrgJoinerData.set(result.newOrgJoiner);
        this.newStoreCreatedData.set(result.newStoreCreated);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.apiErrors.notify(err);
      }
    });
  }

  private buildParams(): StatisticsParams {
    return {
      store: '',
      fromDate: this.dateRangeState.fromDate(),
      toDate: this.dateRangeState.toDate(),
    };
  }
}
