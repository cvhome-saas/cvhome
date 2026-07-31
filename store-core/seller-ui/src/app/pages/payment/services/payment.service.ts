import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {CrudService} from '../../shared/services/crud.service';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  constructor(
    private crudService: CrudService
  ) {
  }

  getTransactions(params): Observable<any> {
    return this.crudService.get(`/spg/payment/api/v1/private/payment/transactions`, params);
  }

  getSupportedPaymentTypes(): Observable<string[]> {
    return this.crudService.get(`/spg/payment/api/v1/private/payment-configuration/supported-payment-types`);
  }

  getSupportedPaymentStatuses(): Observable<string[]> {
    return this.crudService.get(`/spg/payment/api/v1/private/payment-configuration/supported-payment-statuses`);
  }

  approveTransaction(internalRef, param): Observable<any> {
    return this.crudService.post(`/spg/payment/api/v1/private/payment/transaction/${internalRef}/approve`, param);
  }

  rejectTransaction(internalRef): Observable<any> {
    return this.crudService.post(`/spg/payment/api/v1/private/payment/transaction/${internalRef}/reject`, {});
  }

}
