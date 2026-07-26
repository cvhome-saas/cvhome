import {inject, Injectable, signal} from '@angular/core';
import {NbDialogService, NbToastrService} from '@nebular/theme';
import {ActivatedRoute, Router} from '@angular/router';
import {zip} from 'rxjs';
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

@Injectable({providedIn: 'root'})
export class OrderDetailsFacade {
  private readonly ordersService = inject(OrdersService);
  private readonly storeService = inject(StoreService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly errorService = inject(ErrorService);
  private readonly toastr = inject(NbToastrService);
  private readonly dialogService = inject(NbDialogService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly mapper = inject(OrderDetailsMapper);

  statusList = ORDER_STATUS_LIST;
  languages = LANGUAGES;
  orderID = signal<any>(null);
  transactionType = signal<string>('');

  // State signals
  orderDetailsData = signal<any>(null);
  loader = signal<boolean>(false);
  store = signal<any>(null);
  shippingCountry = signal<Array<any>>([]);
  billingCountry = signal<Array<any>>([]);
  shippingStateData = signal<Array<any>>([]);
  billingStateData = signal<Array<any>>([]);
  historyListData = signal<Array<any>>([]);
  transactionListData = signal<Array<any>>([]);

  info = signal({userName: '', language: '', emailAddress: '', datePurchased: ''});
  billing = signal({
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
  shipping = signal({
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

  init(route?: ActivatedRoute) {
    const actRoute = route || this.activatedRoute;
    this.loader.set(true);
    zip([this.selectedStoreService.current(), actRoute.params])
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
    this.ordersService.getOrderDetails(this.orderID).subscribe({
      next: (data) => {
        this.orderDetailsData.set(data);
        this.loader.set(false);
        this.info.set({
          userName: data.customer.userName,
          language: data.customer.language,
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
    this.ordersService.getHistory(this.orderID).subscribe({
      next: (data) => this.historyListData.set(data),
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }

  onBillingChange(value: any, flag: number) {
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

  onShippingChange(value: any, flag: number) {
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
      next: (data) => {
        this.loader.set(false);
        this.toastr.success("History Status has been submitted successfully");
        this.statusFields.set({comments: '', status: ''});
      },
      error: (err) => {
        this.loader.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  updateBilling(billing: any) {
    this.billing.set(billing);
  }

  updateShipping(shipping: any) {
    this.shipping.set(shipping);
  }

  updateInfo(info: any) {
    this.info.set(info);
  }

  updateStatusFields(statusFields: any) {
    this.statusFields.set(statusFields);
  }

  save() {
    this.loader.set(true);
    const param = this.mapper.mapUpdateOrderPayload(this.info(), this.billing(), this.shipping());
    this.ordersService.updateOrder(this.orderID(), param).subscribe({
      next: (data) => {
        this.loader.set(false);
        this.toastr.success("Order has been updated successfully");
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
      next: (data) => {
        this.loader.set(false);
        this.toastr.success("Order has been refunded successfully");
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
      next: (data) => {
        this.loader.set(false);
        this.toastr.success("Order has been captured successfully");
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

  showDialog(value: OrderDialogType | number | string, orderDetailsData?: any, historyListData?: any, transactionListData?: any, store?: any) {
    const val = value;
    const detailsData = orderDetailsData ?? this.orderDetailsData();
    const historyData = historyListData ?? this.historyListData();
    const transactionData = transactionListData ?? this.transactionListData();
    const storeData = store ?? this.store();

    if (val == OrderDialogType.Transaction || val == 1 || val == '1') {
      this.dialogService.open(OrderTransactionComponent, {
        context: {transactionData: transactionData},
      });
    } else if (val == OrderDialogType.Invoice || val == 2 || val == '2') {
      this.dialogService.open(OrderInvoiceComponent, {
        context: {orderData: detailsData, store: storeData},
      });
    } else if (val == OrderDialogType.History || val == 3 || val == '3') {
      this.dialogService.open(OrderHistoryComponent, {
        context: {historyData: historyData},
      });
    }
  }
}
