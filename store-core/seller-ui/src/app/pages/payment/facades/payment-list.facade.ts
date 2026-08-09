import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {NbDialogService} from '@nebular/theme';
import {Observable, tap} from 'rxjs';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {PageEvent} from '@swimlane/ngx-datatable';
import {PaymentService} from 'seller-core/payments';
import {SelectedStoreService} from 'seller-core';
import {ApiErrorService} from 'seller-core';
import {NotificationService} from '../../../core/notifications/notification.service';
import {TableStateService} from 'seller-core';
import {StorePageRequest, PageT} from 'seller-core';
import {PaymentApproveComponent} from '../payment-approve/payment-approve';
import {ACTIONABLE_PAYMENT_STATUSES, EMPTY_PAYMENT_FILTER} from '../constants/payment.constants';
import {PaymentTransaction} from 'seller-core/payments';

export interface PaymentFilter {
  status?: string;
  paymentType?: string;
  requestRef?: string;
  internalRef?: string;
  transactionDateFrom?: string;
  transactionDateTo?: string;
}

export interface PaymentFilterPageRequest extends StorePageRequest, PaymentFilter {
}

@Injectable()
export class PaymentListFacade {
  private readonly paymentService = inject(PaymentService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly notify = inject(NotificationService);
  private readonly dialogService = inject(NbDialogService);
  readonly tableState = inject(TableStateService<PaymentTransaction, PaymentFilterPageRequest>);

  readonly store = signal<string>('');
  readonly paymentTypes = signal<string[]>([]);
  readonly paymentStatuses = signal<string[]>([]);

  init(destroyRef: DestroyRef): void {
    this.loadFilterOptions();

    this.selectedStoreService.current()
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
        next: (store) => {
          this.store.set(store || '');
          if (store) {
            this.refresh();
          }
        },
        error: (err) => this.apiErrors.notify(err)
      });
  }

  onPageChange(event: PageEvent): void {
    this.tableState.patchParams({page: event.offset});
    this.refresh();
  }

  onFilterChange(filters: PaymentFilter): void {
    this.tableState.patchParams(this.mapFilterRequest(filters));
    this.refresh();
  }

  filter(): PaymentFilter {
    return {...EMPTY_PAYMENT_FILTER};
  }

  canApproveOrReject(row: PaymentTransaction): boolean {
    return ACTIONABLE_PAYMENT_STATUSES.includes(row.status);
  }

  approve(row: PaymentTransaction): void {
    this.dialogService.open(PaymentApproveComponent).onClose.subscribe((transactionNo) => {
      if (!transactionNo) {
        return;
      }
      this.paymentService.approveTransaction(row.internalRef, {transactionNo}).subscribe({
        next: () => {
          this.notify.success('TRANSACTION.APPROVE_SUCCESS');
          this.refresh();
        },
        error: (err) => this.apiErrors.notify(err)
      });
    });
  }

  reject(row: PaymentTransaction): void {
    this.paymentService.rejectTransaction(row.internalRef).subscribe({
      next: () => {
        this.notify.success('TRANSACTION.REJECT_SUCCESS');
        this.refresh();
      },
      error: (err) => this.apiErrors.notify(err)
    });
  }

  private mapFilterRequest(filters: PaymentFilter): Partial<PaymentFilterPageRequest> {
    return {
      ...filters,
      transactionDateFrom: filters.transactionDateFrom ? `${filters.transactionDateFrom}T00:00:00Z` : '',
      transactionDateTo: filters.transactionDateTo ? `${filters.transactionDateTo}T23:59:59Z` : ''
    };
  }

  private loadFilterOptions(): void {
    this.paymentService.getSupportedPaymentTypes().subscribe({
      next: (types) => this.paymentTypes.set(types),
      error: (err) => this.apiErrors.notify(err)
    });
    this.paymentService.getSupportedPaymentStatuses().subscribe({
      next: (statuses) => this.paymentStatuses.set(statuses),
      error: (err) => this.apiErrors.notify(err)
    });
  }

  private refresh(): void {
    const store = this.store();
    if (!store) {
      return;
    }

    const request: PaymentFilterPageRequest = {...this.tableState.params(), store};
    this.loadTransactions(request).subscribe(page => this.tableState.setPage(page));
  }

  private loadTransactions(request: PaymentFilterPageRequest): Observable<PageT<PaymentTransaction>> {
    this.tableState.setLoading(true);
    return this.paymentService.getTransactions(request).pipe(
      tap({
        next: () => this.tableState.setLoading(false),
        error: (err) => {
          this.tableState.setLoading(false);
          this.apiErrors.notify(err);
        }
      })
    );
  }
}
