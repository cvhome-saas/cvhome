import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {CrudService} from '../../shared/services/crud.service';

@Injectable({
  providedIn: 'root'
})
export class OrdersService {

  constructor(
    private crudService: CrudService
  ) {
  }

  getOrders(params): Observable<any> {
    return this.crudService.get(`/spg/checkout/api/v1/private/orders`, params);
  }

  getOrderDetails(orderID): Observable<any> {
    return this.crudService.get(`/spg/checkout/api/v1/private/orders/${orderID}`);
  }

  getCountry(): Observable<any> {
    return this.crudService.get(`/spg/checkout/api/v1/country`)
  }

  getBillingZone(value): Observable<any> {
    return this.crudService.get(`/spg/checkout/api/v1/zones?code=${value}`)
  }

  getHistory(orderID): Observable<any> {
    return this.crudService.get(`/spg/checkout/api/v1/private/orders/${orderID}/history`)
  }

  addHistory(orderID, param): Observable<any> {
    return this.crudService.post(`/spg/checkout/api/v1/private/orders/${orderID}/history`, param);
  }

  updateOrder(orderID, param): Observable<any> {
    return this.crudService.patch(`/spg/checkout/api/v1/private/orders/${orderID}/customer`, param);
  }

  refundOrder(orderID): Observable<any> {
    return this.crudService.post(`/spg/checkout/api/v1/private/orders/${orderID}/refund`, {});
  }

  captureOrder(orderID): Observable<any> {
    return this.crudService.post(`/spg/checkout/api/v1/private/orders/${orderID}/capture`, {});
  }

}
