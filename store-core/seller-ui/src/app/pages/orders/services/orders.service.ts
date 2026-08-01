import { Injectable, inject } from '@angular/core';
import {Observable} from 'rxjs';
import {CrudService} from '../../shared/services/crud.service';
import {PageT, StorePageRequest} from '../../common/BaseTable';

@Injectable({
  providedIn: 'root'
})
export class OrdersService {
  private readonly crudService = inject(CrudService);


  getOrders(params: StorePageRequest): Observable<PageT<any>> {
    return this.crudService.get(`/spg/checkout/api/v1/private/orders`, params);
  }

  getOrderDetails(orderID: number | string): Observable<any> {
    return this.crudService.get(`/spg/checkout/api/v1/private/orders/${orderID}`);
  }

  getCountry(): Observable<any> {
    return this.crudService.get(`/spg/checkout/api/v1/country`)
  }

  getBillingZone(value: string): Observable<any> {
    return this.crudService.get(`/spg/checkout/api/v1/zones?code=${value}`)
  }

  getHistory(orderID: number | string): Observable<any> {
    return this.crudService.get(`/spg/checkout/api/v1/private/orders/${orderID}/history`)
  }

  addHistory(orderID: number | string, param: any): Observable<any> {
    return this.crudService.post(`/spg/checkout/api/v1/private/orders/${orderID}/history`, param);
  }

  updateOrder(orderID: number | string, param: any): Observable<any> {
    return this.crudService.patch(`/spg/checkout/api/v1/private/orders/${orderID}/customer`, param);
  }

  refundOrder(orderID: number | string): Observable<any> {
    return this.crudService.post(`/spg/checkout/api/v1/private/orders/${orderID}/refund`, {});
  }

  captureOrder(orderID: number | string): Observable<any> {
    return this.crudService.post(`/spg/checkout/api/v1/private/orders/${orderID}/capture`, {});
  }

}
