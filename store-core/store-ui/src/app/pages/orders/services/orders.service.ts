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

  getOrders(store, params): Observable<any> {
    // const params = {
    //   'count': '50',
    //   'start': '0'
    // };
    return this.crudService.get(`/store/api/v1/private/orders?store=${store}`, params);
  }

  getOrderDetails(store, orderID): Observable<any> {
    return this.crudService.get(`/store/api/v1/private/orders/${orderID}?store=${store}`);
  }

  getCountry(store): Observable<any> {
    return this.crudService.get(`/store/api/v1/country?store=${store}`)
  }

  getBillingZone(store, value): Observable<any> {
    return this.crudService.get(`/store/api/v1/zones?code=${value}&store=${store}`)
  }

  getShippingZone(store, value): Observable<any> {
    return this.crudService.get(`/store/api/v1/zones?code=${value}&store=${store}`)
  }

  getHistory(store, orderID): Observable<any> {
    return this.crudService.get(`/store/api/v1/private/orders/${orderID}/history?store=${store}`)
  }

  addHistory(store, orderID, param): Observable<any> {
    return this.crudService.post(`/store/api/v1/private/orders/${orderID}/history?store=${store}`, param);
  }

  updateOrder(store, orderID, param): Observable<any> {
    return this.crudService.patch(`/store/api/v1/private/orders/${orderID}/customer?store=${store}`, param);
  }

  getNextTransaction(store, orderID): Observable<any> {
    return this.crudService.get(`/store/api/v1/private/orders/${orderID}/payment/nextTransaction?store=${store}`);
  }

  refundOrder(store, orderID): Observable<any> {
    return this.crudService.post(`/store/api/v1/private/orders/${orderID}/refund?store=${store}`, {});
  }

  captureOrder(store, orderID): Observable<any> {
    return this.crudService.post(`/store/api/v1/private/orders/${orderID}/capture?store=${store}`, {});
  }

  getTransactions(store, orderID): Observable<any> {
    return this.crudService.get(`/store/api/v1/private/orders/${orderID}/payment/transactions?store=${store}`);
  }
}
