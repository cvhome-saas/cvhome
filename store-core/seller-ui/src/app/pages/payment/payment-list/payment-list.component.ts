import {Component, OnInit} from '@angular/core';
import {PaymentService} from "../services/payment.service";
import {ColumnMode} from "@swimlane/ngx-datatable";
import {ErrorService} from "../../shared/services/error.service";
import {SelectedStoreService} from "../../shared/services/selected-store.service";
import {BaseTable, PageT, StorePageRequest} from "../../common/BaseTable";
import {forkJoin, Observable, of} from "rxjs";
import {NbDialogService} from "@nebular/theme";
import {PaymentApproveComponent} from "../payment-approve/payment-approve";

export interface PaymentFilterPageRequest extends StorePageRequest {
  status?: string;
  paymentType?: string;
  requestRef?: string;
  internalRef?: string;
  transactionDateFrom?: string;
  transactionDateTo?: string;
}

const PENDING_STATUSES = ['PENDING', 'PROCESSING', 'WAITING_VERIFICATION', 'AUTHORIZED'];

@Component({
  selector: 'ngx-payment-list',
  standalone: false,
  templateUrl: './payment-list.component.html',
  styleUrls: ['./payment-list.component.scss']
})
export class PaymentListComponent extends BaseTable<any> implements OnInit {
  filter = {
    status: '',
    paymentType: '',
    requestRef: '',
    internalRef: '',
    transactionDateFrom: '',
    transactionDateTo: ''
  };
  paymentTypes: string[] = [];
  paymentStatuses: string[] = [];
  protected readonly ColumnMode = ColumnMode;
  private isInitialized: boolean = false;

  constructor(
    private paymentService: PaymentService,
    private dialogService: NbDialogService,
    errorService: ErrorService,
    selectedStoreService: SelectedStoreService
  ) {
    super(selectedStoreService, errorService)
  }

  ngOnInit(): void {
    this.isInitialized = true;
    this.trigger();
    this.loadFilterOptions();
  }

  loadFilterOptions(): void {
    forkJoin({
      paymentTypes: this.paymentService.getSupportedPaymentTypes(),
      paymentStatuses: this.paymentService.getSupportedPaymentStatuses()
    }).subscribe({
      next: ({paymentTypes, paymentStatuses}) => {
        this.paymentTypes = paymentTypes;
        this.paymentStatuses = paymentStatuses;
      },
      error: (err) => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  override list(request: PaymentFilterPageRequest): Observable<PageT<any>> {
    if (!super.params.store || !this.isInitialized) {
      return of();
    }
    Object.assign(request, this.filter);
    if (request.transactionDateFrom) {
      request.transactionDateFrom = `${request.transactionDateFrom}T00:00:00Z`;
    }
    if (request.transactionDateTo) {
      request.transactionDateTo = `${request.transactionDateTo}T23:59:59Z`;
    }
    return this.paymentService.getTransactions(request);
  }

  onFilterChange(): void {
    this.trigger();
  }

  resetFilters(): void {
    this.filter = {
      status: '',
      paymentType: '',
      requestRef: '',
      internalRef: '',
      transactionDateFrom: '',
      transactionDateTo: ''
    };
    this.trigger();
  }

  canApproveOrReject(row): boolean {
    return PENDING_STATUSES.includes(row.status);
  }

  approve(row): void {
    this.dialogService.open(PaymentApproveComponent).onClose.subscribe((transactionNo) => {
      if (!transactionNo) {
        return;
      }
      this.paymentService.approveTransaction(row.internalRef, {transactionNo}).subscribe({
        next: () => {
          this.errorService.success('TRANSACTION.APPROVE_SUCCESS');
          this.trigger();
        },
        error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });
    });
  }

  reject(row): void {
    this.paymentService.rejectTransaction(row.internalRef).subscribe({
      next: () => {
        this.errorService.success('TRANSACTION.REJECT_SUCCESS');
        this.trigger();
      },
      error: (err) => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

}
