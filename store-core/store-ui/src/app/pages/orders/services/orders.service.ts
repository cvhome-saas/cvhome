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

  getOrders(storeId, params): Observable<any> {
    // const params = {
    //   'count': '50',
    //   'start': '0'
    // };
    return this.crudService.get(`/store/api/v1/private/orders?store=${storeId}`, params);
  }

  getOrderDetails(storeId, orderID): Observable<any> {
    return this.crudService.get(`/store/api/v1/private/orders/${orderID}?store=${storeId}`);
  }

  getCountry(storeId): Observable<any> {
    return this.crudService.get(`/store/api/v1/country?store=${storeId}`)
  }

  getBillingZone(storeId, value): Observable<any> {
    return this.crudService.get(`/store/api/v1/zones?code=${value}&store=${storeId}`)
  }

  getShippingZone(storeId, value): Observable<any> {
    return this.crudService.get(`/store/api/v1/zones?code=${value}&store=${storeId}`)
  }

  getHistory(storeId, orderID): Observable<any> {
    return this.crudService.get(`/store/api/v1/private/orders/${orderID}/history?store=${storeId}`)
  }

  addHistory(storeId, orderID, param): Observable<any> {
    return this.crudService.post(`/store/api/v1/private/orders/${orderID}/history?store=${storeId}`, param);
  }

  updateOrder(storeId, orderID, param): Observable<any> {
    return this.crudService.patch(`/store/api/v1/private/orders/${orderID}/customer?store=${storeId}`, param);
  }

  getNextTransaction(storeId, orderID): Observable<any> {
    return this.crudService.get(`/store/api/v1/private/orders/${orderID}/payment/nextTransaction?store=${storeId}`);
  }

  refundOrder(storeId, orderID): Observable<any> {
    return this.crudService.post(`/store/api/v1/private/orders/${orderID}/refund?store=${storeId}`, {});
  }

  captureOrder(storeId, orderID): Observable<any> {
    return this.crudService.post(`/store/api/v1/private/orders/${orderID}/capture?store=${storeId}`, {});
  }

  getTransactions(storeId, orderID): Observable<any> {
    return this.crudService.get(`/store/api/v1/private/orders/${orderID}/payment/transactions?store=${storeId}`);
  }
}
