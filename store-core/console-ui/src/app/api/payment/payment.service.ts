/** Ported from seller-ui/projects/seller-core/payments/src/lib/services/payment.service.ts. */
import {Injectable, inject} from '@angular/core';
import {Observable, map} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {PageT} from '@core/table/table.types';
import type {PaymentTransaction, PaymentStatus, TransactionQuery} from '@models/payment';

/**
 * Ported from seller-ui/projects/seller-core/payments/src/lib/services/payment.service.ts — the read
 * half only. `approveTransaction`, `rejectTransaction` and the supported-type lookups belong to the
 * payments module and are not needed to count a queue.
 */
const PAYMENT_API_BASE = '/spg/payment/api/v1/private/payment';

@Injectable({providedIn: 'root'})
export class PaymentService {
  private readonly crudService = inject(CrudService);

  transactions(query: TransactionQuery & {page: number; count: number}): Observable<PageT<PaymentTransaction>> {
    return this.crudService.get(`${PAYMENT_API_BASE}/transactions`, {...query});
  }

  /**
   * How many transactions sit in one status.
   *
   * Asks for a single row and reads `totalElements`, because there is no count endpoint — the only way
   * to learn how many of something there are is to fetch some of them. Cheap at one row, but see
   * lessons.md, "Dashboard — counting requires fetching".
   */
  countByStatus(status: PaymentStatus): Observable<number> {
    return this.transactions({status, page: 0, count: 1}).pipe(map((page) => page.totalElements));
  }
}
