import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {forkJoin} from 'rxjs';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {DateRangeStateService} from '../state/date-range.state';
import {ApiErrorService} from '../../../core/errors/api-error.service';
import {SelectedStoreService} from '../../shared/services/selected-store.service';
import {EMPTY_STATISTIC_LIST, StatisticApiService, StatisticList, StatisticsParams} from '../services/statistic.api.service';

@Injectable()
export class ClientDashboardFacade {
  private readonly dateRangeState = inject(DateRangeStateService);
  private readonly statisticApi = inject(StatisticApiService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly apiErrors = inject(ApiErrorService);

  readonly fromDate = this.dateRangeState.fromDate;
  readonly toDate = this.dateRangeState.toDate;
  readonly fromMaxDate = new Date();
  readonly toMaxDate = new Date();

  readonly store = signal<string>('');
  readonly loading = signal<boolean>(false);
  readonly ordersData = signal<StatisticList>(EMPTY_STATISTIC_LIST);
  readonly customerCountriesData = signal<StatisticList>(EMPTY_STATISTIC_LIST);
  readonly productsData = signal<StatisticList>(EMPTY_STATISTIC_LIST);

  init(destroyRef: DestroyRef): void {
    this.selectedStoreService.current()
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
        next: (store) => {
          this.store.set(store || '');
          this.loadAll();
        },
        error: (err) => this.apiErrors.notify(err)
      });
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
    const store = this.store();
    if (!store) return;

    const params = this.buildParams(store);
    this.loading.set(true);

    forkJoin({
      orders: this.statisticApi.getOrderStatistic(params),
      customerCountries: this.statisticApi.getCustomerStatistic(params),
      products: this.statisticApi.getProductStatistic(params),
    }).subscribe({
      next: (result) => {
        this.ordersData.set(result.orders);
        this.customerCountriesData.set(result.customerCountries);
        this.productsData.set(result.products);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.apiErrors.notify(err);
      }
    });
  }

  private buildParams(store: string): StatisticsParams {
    return {
      store,
      fromDate: this.dateRangeState.fromDate(),
      toDate: this.dateRangeState.toDate(),
    };
  }
}
