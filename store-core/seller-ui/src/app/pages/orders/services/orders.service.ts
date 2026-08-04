import { Injectable, inject } from '@angular/core';
import {Observable} from 'rxjs';
import {CrudService} from '../../shared/services/crud.service';
import {PageT, StorePageRequest} from '../../shared/table/table.types';
import {
  PersistableOrderStatusHistory,
  ReadableCountry,
  ReadableOrder,
  ReadableOrderStatusHistory,
  ReadableZone,
  UpdateOrderPayload
} from '../models/order.model';

@Injectable({
  providedIn: 'root'
})
export class OrdersService {
  private readonly crudService = inject(CrudService);


  getOrders(params: StorePageRequest): Observable<PageT<ReadableOrder>> {
    return this.crudService.get(`/spg/checkout/api/v1/private/orders`, params);
  }

  getOrderDetails(orderID: number | string): Observable<ReadableOrder> {
    return this.crudService.get(`/spg/checkout/api/v1/private/orders/${orderID}`);
  }

  getCountry(): Observable<ReadableCountry[]> {
    return this.crudService.get(`/spg/checkout/api/v1/country`)
  }

  getBillingZone(value: string): Observable<ReadableZone[]> {
    return this.crudService.get(`/spg/checkout/api/v1/zones?code=${value}`)
  }

  getHistory(orderID: number | string): Observable<ReadableOrderStatusHistory[]> {
    return this.crudService.get(`/spg/checkout/api/v1/private/orders/${orderID}/history`)
  }

  addHistory(orderID: number | string, param: PersistableOrderStatusHistory): Observable<void> {
    return this.crudService.post(`/spg/checkout/api/v1/private/orders/${orderID}/history`, param);
  }

  updateOrder(orderID: number | string, param: UpdateOrderPayload): Observable<void> {
    return this.crudService.patch(`/spg/checkout/api/v1/private/orders/${orderID}/customer`, param);
  }

  refundOrder(orderID: number | string): Observable<void> {
    return this.crudService.post(`/spg/checkout/api/v1/private/orders/${orderID}/refund`, {});
  }

  captureOrder(orderID: number | string): Observable<void> {
    return this.crudService.post(`/spg/checkout/api/v1/private/orders/${orderID}/capture`, {});
  }

}
