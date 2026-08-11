import { Injectable, inject } from '@angular/core';
import {Observable} from 'rxjs';
import {CrudService} from 'seller-core';

/** Base path for tenancy's statistic endpoints. The order/customer/product statistics below are a pod
 *  concern and go through spg instead, which is why only some paths use this. */
export const TENANCY_STATISTIC_API_BASE = '/tenancy/api/v2/private';

@Injectable({
  providedIn: 'root'
})
export class StatisticApiService {
  private readonly crudService = inject(CrudService);


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
    return this.crudService.post(`${TENANCY_STATISTIC_API_BASE}/store-statistic`, params);
  }

  getNewOrgJoinerStatistic(params: StatisticsParams): Observable<StatisticList> {
    return this.crudService.post(`${TENANCY_STATISTIC_API_BASE}/org-statistic`, params);
  }

  getSubscriptionStatistic(params: StatisticsParams): Observable<StatisticList> {
    return this.crudService.post(`${TENANCY_STATISTIC_API_BASE}/subscription-statistic`, params);
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
