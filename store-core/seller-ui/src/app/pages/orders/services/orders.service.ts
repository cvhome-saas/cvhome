import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {CrudService} from '../../../shared/services/crud.service';

@Injectable({
  providedIn: 'root'
})
export class OrdersService {

  constructor(
    private crudService: CrudService
  ) {
  }

  getOrders(params): Observable<any> {
    return this.crudService.get(`/store-pod-gateway/order/api/v1/private/orders`, params);
  }

  getOrderDetails(store, orderID): Observable<any> {
    return this.crudService.get(`/store-pod-gateway/order/api/v1/private/orders/${orderID}?store=${store}`);
  }

  getCountry(store): Observable<any> {
    return this.crudService.get(`/store-pod-gateway/order/api/v1/country?store=${store}`)
  }

  getBillingZone(store, value): Observable<any> {
    return this.crudService.get(`/store-pod-gateway/order/api/v1/zones?code=${value}&store=${store}`)
  }

  getHistory(store, orderID): Observable<any> {
    return this.crudService.get(`/store-pod-gateway/order/api/v1/private/orders/${orderID}/history?store=${store}`)
  }

  addHistory(store, orderID, param): Observable<any> {
    return this.crudService.post(`/store-pod-gateway/order/api/v1/private/orders/${orderID}/history?store=${store}`, param);
  }

  updateOrder(store, orderID, param): Observable<any> {
    return this.crudService.patch(`/store-pod-gateway/order/api/v1/private/orders/${orderID}/customer?store=${store}`, param);
  }

  refundOrder(store, orderID): Observable<any> {
    return this.crudService.post(`/store-pod-gateway/order/api/v1/private/orders/${orderID}/refund?store=${store}`, {});
  }

  captureOrder(store, orderID): Observable<any> {
    return this.crudService.post(`/store-pod-gateway/order/api/v1/private/orders/${orderID}/capture?store=${store}`, {});
  }

}
