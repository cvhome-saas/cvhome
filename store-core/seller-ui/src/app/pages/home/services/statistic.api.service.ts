import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {CrudService} from '../../shared/services/crud.service';

@Injectable({
  providedIn: 'root'
})
export class StatisticApiService {

  constructor(private readonly crudService: CrudService) {
  }

  getOrderStatistic(params: StatisticsParams): Observable<StatisticList> {
    return this.crudService.post(`/spg/checkout/api/v2/private/order-statistic`, params);
  }

  getCustomerStatistic(params: StatisticsParams): Observable<StatisticList> {
    return this.crudService.post(`/spg/checkout/api/v2/private/customer-statistic`, params);
  }

  getProductStatistic(params: StatisticsParams): Observable<StatisticList> {
    return this.crudService.post(`/spg/checkout/api/v2/private/product-statistic`, params);
  }

  getNewStoreCreatedStatistic(params: StatisticsParams): Observable<StatisticList> {
    return this.crudService.post(`/control-plane/api/v2/private/store-statistic`, params);
  }

  getNewOrgJoinerStatistic(params: StatisticsParams): Observable<StatisticList> {
    return this.crudService.post(`/control-plane/api/v2/private/org-statistic`, params);
  }

  getSubscriptionStatistic(params: StatisticsParams): Observable<StatisticList> {
    return this.crudService.post(`/control-plane/api/v2/private/subscription-statistic`, params);
  }
}

export interface StatisticList {
  entries: StatisticEntry[];
}

export interface StatisticEntry {
  date: string
  name: string
  value: number
}

export interface StatisticsParams {
  store: string
  fromDate: Date
  toDate: Date
}

export const EMPTY_STATISTIC_LIST: StatisticList = {entries: []};
