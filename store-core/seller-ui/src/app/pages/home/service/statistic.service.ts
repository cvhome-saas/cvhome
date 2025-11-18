import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {CrudService} from '../../../shared/services/crud.service';

@Injectable({
  providedIn: 'root'
})
export class StatisticService {

  constructor(private crudService: CrudService) {
  }

  getOrderStatistic(params: StatisticsParams): Observable<StatisticList> {
    return this.crudService.post(`/store-pod-gateway/order/api/v2/private/order-statistic?store=${params.store}`, params);
  }

  getCustomerStatistic(params: StatisticsParams): Observable<StatisticList> {
    return this.crudService.post(`/store-pod-gateway/order/api/v2/private/customer-statistic?store=${params.store}`, params);
  }

  getProductStatistic(params: StatisticsParams): Observable<StatisticList> {
    return this.crudService.post(`/store-pod-gateway/order/api/v2/private/product-statistic?store=${params.store}`, params);
  }

  getNewStoreCreatedStatistic(params: StatisticsParams) {
    return this.crudService.post(`/control-plane/api/v2/private/store-statistic`, params);
  }

  getNewOrgJoinerStatistic(params: StatisticsParams) {
    return this.crudService.post(`/control-plane/api/v2/private/org-statistic`, params);
  }

  getSubscriptionStatistic(params: StatisticsParams) {
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
