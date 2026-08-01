import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';
import {CrudService} from '../../shared/services/crud.service';
import {PageT, StorePageRequest} from '../../common/BaseTable';
import {PaymentTransaction} from '../models/payment-transaction.model';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private readonly crudService = inject(CrudService);

  getTransactions(params: StorePageRequest): Observable<PageT<PaymentTransaction>> {
    return this.crudService.get(`/spg/payment/api/v1/private/payment/transactions`, params);
  }

  approveTransaction(internalRef: string, param: {transactionNo: string}): Observable<void> {
    return this.crudService.post(`/spg/payment/api/v1/private/payment/transaction/${internalRef}/approve`, param);
  }

  rejectTransaction(internalRef: string): Observable<void> {
    return this.crudService.post(`/spg/payment/api/v1/private/payment/transaction/${internalRef}/reject`, {});
  }

  getSupportedPaymentTypes(): Observable<string[]> {
    return this.crudService.get(`/spg/payment/api/v1/private/payment-configuration/supported-payment-types`);
  }

  getSupportedPaymentStatuses(): Observable<string[]> {
    return this.crudService.get(`/spg/payment/api/v1/private/payment-configuration/supported-payment-statuses`);
  }

}
