import {DestroyRef, inject, Injectable, signal} from '@angular/core';
import {NbDialogService} from '@nebular/theme';
import {ActivatedRoute, Router} from '@angular/router';
import {zip} from 'rxjs';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {OrderDetailsMapper} from '../services/order-details.mapper';
import {AsYouType} from 'libphonenumber-js';
import {LANGUAGES, ORDER_STATUS_LIST, OrderDialogType} from '../constants/order-details.constants';
import {OrdersService} from "../../services/orders.service";
import {ErrorService} from "../../../shared/services/error.service";
import {SelectedStoreService} from "../../../shared/services/selected-store.service";
import {StoreService} from "../../../store-management/services/store.service";
import {OrderTransactionComponent} from "../../order-transaction/order-transaction";
import {OrderInvoiceComponent} from "../../order-invoice/order-invoice";
import {OrderHistoryComponent} from "../../order-history/order-history";
import {CustomerAddress, ReadableCountry, ReadableOrder, ReadableOrderStatusHistory, ReadableZone} from "../../models/order.model";
import {Store} from "../../../store-management/models/store";

@Injectable()
export class OrderDetailsFacade {
  private readonly ordersService = inject(OrdersService);
  private readonly storeService = inject(StoreService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly errorService = inject(ErrorService);
  private readonly dialogService = inject(NbDialogService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly mapper = inject(OrderDetailsMapper);

  statusList = ORDER_STATUS_LIST;
  languages = LANGUAGES;
  orderID = signal<string>(null);
  transactionType = signal<string>('');

  // State signals
  orderDetailsData = signal<ReadableOrder>(null);
  loader = signal<boolean>(false);
  store = signal<Store>(null);
  shippingCountry = signal<ReadableCountry[]>([]);
  billingCountry = signal<ReadableCountry[]>([]);
  shippingStateData = signal<ReadableZone[]>([]);
  billingStateData = signal<ReadableZone[]>([]);
  historyListData = signal<ReadableOrderStatusHistory[]>([]);
  transactionListData = signal<unknown[]>([]);

  info = signal({userName: '', language: '', emailAddress: '', datePurchased: ''});
  billing = signal<CustomerAddress>({
    firstName: '',
    lastName: '',
    company: '',
    address: '',
    city: '',
    zone: '',
    postalCode: '',
    phone: '',
    country: ''
  });
  shipping = signal<CustomerAddress>({
    firstName: '',
    lastName: '',
    company: '',
    address: '',
    city: '',
    zone: '',
    postalCode: '',
    phone: '',
    country: ''
  });
  statusFields = signal({comments: '', status: ''});

  init(destroyRef: DestroyRef): void {
    this.loader.set(true);
    zip([this.selectedStoreService.current(), this.activatedRoute.params])
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
        next: ([selectedStore, params]) => {
          this.orderID.set(params.id);
          this.loadAll(selectedStore);
        },
        error: (err) => {
          this.loader.set(false);
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });
  }

  private loadAll(storeID: string) {
    this.getStore(storeID);
    this.getCountry();
    this.getOrderDetails();
    this.getHistory();
  }

  private getStore(storeID: string) {
    this.storeService.getStore(storeID).subscribe({
      next: (data) => this.store.set(data),
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }

  private getCountry() {
    this.ordersService.getCountry().subscribe({
      next: (data) => {
        this.shippingCountry.set(data);
        this.billingCountry.set(data);
      },
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }

  private getOrderDetails() {
    this.ordersService.getOrderDetails(this.orderID()).subscribe({
      next: (data) => {
        this.orderDetailsData.set(data);
        this.loader.set(false);
        this.info.set({
          userName: data.customer.username,
          language: '',
          emailAddress: data.customer.emailAddress,
          datePurchased: data.datePurchased
        });
        this.billing.set(data.billing);
        if (data.delivery) {
          this.shipping.set(data.delivery);
          this.onShippingChange(data.delivery.country, 0);
        }
        this.onBillingChange(data.billing.country, 0);
      },
      error: (err) => {
        this.loader.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  private getHistory() {
    this.ordersService.getHistory(this.orderID()).subscribe({
      next: (data) => this.historyListData.set(data),
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }

  onBillingChange(value: string, flag: number) {
    this.ordersService.getBillingZone(value).subscribe({
      next: (data) => {
        this.billingStateData.set(data);
        if (flag === 1 && data.length > 0) {
          this.billing.update(b => ({...b, zone: data[0].code}));
        } else if (flag === 1) {
          this.billing.update(b => ({...b, zone: ''}));
        }
      },
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }

  onShippingChange(value: string, flag: number) {
    this.ordersService.getBillingZone(value).subscribe({
      next: (data) => {
        this.shippingStateData.set(data);
        if (flag === 1 && data.length > 0) {
          this.shipping.update(s => ({...s, zone: data[0].code}));
        } else if (flag === 1) {
          this.shipping.update(s => ({...s, zone: ''}));
        }
      },
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }

  updateHistory() {
    this.loader.set(true);
    const param = this.mapper.mapHistoryPayload(this.statusFields().comments, this.statusFields().status);
    this.ordersService.addHistory(this.orderID(), param).subscribe({
      next: () => {
        this.loader.set(false);
        this.errorService.success("History Status has been submitted successfully");
        this.statusFields.set({comments: '', status: ''});
      },
      error: (err) => {
        this.loader.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  save() {
    this.loader.set(true);
    const param = this.mapper.mapUpdateOrderPayload(this.info(), this.billing(), this.shipping());
    this.ordersService.updateOrder(this.orderID(), param).subscribe({
      next: () => {
        this.loader.set(false);
        this.errorService.success("Order has been updated successfully");
      },
      error: (err) => {
        this.loader.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      },
    });
  }

  onPhoneChange(phone: string) {
    this.billing.update(b => ({...b, phone: new AsYouType('US').input(phone)}));
  }

  onShippingPhoneChange(phone: string) {
    this.shipping.update(s => ({...s, phone: new AsYouType('US').input(phone)}));
  }

  onClickRefund() {
    this.loader.set(true);
    this.ordersService.refundOrder(this.orderID()).subscribe({
      next: () => {
        this.loader.set(false);
        this.errorService.success("Order has been refunded successfully");
      },
      error: (err) => {
        this.loader.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  onClickCapture() {
    this.loader.set(true);
    this.ordersService.captureOrder(this.orderID()).subscribe({
      next: () => {
        this.loader.set(false);
        this.errorService.success("Order has been captured successfully");
      },
      error: (err) => {
        this.loader.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  goBack() {
    this.router.navigate(['pages/orders/order-list']);
  }

  showDialog(value: OrderDialogType | number | string): void {
    if (value == OrderDialogType.Transaction || value == 1 || value == '1') {
      this.dialogService.open(OrderTransactionComponent, {
        context: {transactionData: this.transactionListData()},
      });
    } else if (value == OrderDialogType.Invoice || value == 2 || value == '2') {
      this.dialogService.open(OrderInvoiceComponent, {
        context: {orderData: this.orderDetailsData(), store: this.store()},
      });
    } else if (value == OrderDialogType.History || value == 3 || value == '3') {
      this.dialogService.open(OrderHistoryComponent, {
        context: {historyData: this.historyListData()},
      });
    }
  }
}
