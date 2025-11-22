import {Component, OnInit} from '@angular/core';
import {NbDialogService, NbToastrService} from '@nebular/theme';
import {OrdersService} from '../services/orders.service';
import moment from 'moment';
import {OrderInvoiceComponent} from '../order-invoice/order-invoice';
import {OrderHistoryComponent} from '../order-history/order-history';
import {OrderTransactionComponent} from '../order-transaction/order-transaction';

import {ActivatedRoute, Router} from '@angular/router';
import {AsYouType} from 'libphonenumber-js';
import {ErrorService} from "../../../shared/services/error.service";
import {StoreService} from "../../store-management/services/store.service";
import {SelectedStoreService} from "../../../shared/services/selected-store.service";
import {zip} from "rxjs";

@Component({
  selector: 'ngx-order-details',
  standalone: false,
  templateUrl: './order-details.component.html',
  styleUrls: ['./order-details.component.scss']
})
export class OrderDetailsComponent implements OnInit {

  shippingCountry: Array<any> = []
  shippingStateData: Array<any> = []
  billingStateData: Array<any> = []
  billingCountry: Array<any> = []
  groups: Array<any> = []
  loader = false;
  orderDetailsData: any;
  historyListData: Array<any> = [];
  transactionListData: Array<any> = [];
  statusList: Array<any> = [{'name': 'ORDERED', 'id': 'ORDERED'}, {
    'name': 'PROCESSED',
    'id': 'PROCESSED'
  }, {'name': 'DELIVERED', 'id': 'DELIVERED'}, {'name': 'REFUNDED', 'id': 'REFUNDED'}, {
    'name': 'CANCELED',
    'id': 'CANCELED'
  }]
  info = {
    userName: '',
    language: '',
    emailAddress: '',
    datePurchased: ''
  }
  statusFields = {
    comments: '',
    status: ''
  }
  shipping = {
    phone: '',
    firstName: '',
    lastName: '',
    company: '',
    address: '',
    city: '',
    zone: '',
    tempZone: '',
    country: '',
    tempCountry: '',
    postalCode: '',

  }
  billing = {
    firstName: '',
    lastName: '',
    company: '',
    address: '',
    city: '',
    zone: '',
    tempZone: '',
    country: '',
    tempCountry: '',
    postalCode: '',
    phone: ''
  }
  transactionType: string = ''
  orderID: any;
  storeID: any;
  languages: Array<any> = [{'code': 'en', 'name': 'English'}, {'code': 'fr', 'name': 'French'}]
  store: any;

  constructor(private ordersService: OrdersService, private storeService: StoreService, private toastr: NbToastrService,
              private errorService: ErrorService, private dialogService: NbDialogService, private router: Router,
              private activatedRoute: ActivatedRoute, private selectedStoreService: SelectedStoreService) {

  }

  getOrderDetails() {
    this.loader = true;
    this.ordersService.getOrderDetails(this.orderID)
      .subscribe({
        next: (data) => {
          this.loader = false;
          // console.log(data);
          this.orderDetailsData = data;
          this.onBillingChange(data.billing.country, 0)


          this.info.emailAddress = data.customer.emailAddress;
          this.info.datePurchased = data.datePurchased;

          this.billing = data.billing;
          if (data.delivery) {
            this.onShippingChange(data.delivery.country, 0)
            this.shipping = data.delivery;
          }
        },
        error: (err) => {
          this.loader = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        },
      });
  }

  ngOnInit() {

    zip([this.selectedStoreService.current(), this.activatedRoute.params])
      .subscribe({
        next: ([selectedStore, params]) => {
          this.storeID = selectedStore;
          this.orderID = params.id
          this.getStore();
          this.getCountry();
          this.getOrderDetails();
          this.getHistory();
        },
        error: (err) => {
          this.loader = false;
        },
        complete: () => {
          this.loader = false;
        }
      });
  }

  getHistory() {
    this.ordersService.getHistory(this.orderID)
      .subscribe(data => {
        this.historyListData = data;
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });
  }


  getStore() {
    this.storeService.getStore(this.storeID)
      .subscribe({
        next: (data) => {
          this.store = data;
        }, error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      })

  }

  getCountry() {
    this.ordersService.getCountry()
      .subscribe({
        next: (data) => {
          this.shippingCountry = data;
          this.billingCountry = data;
        }, error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      })
  }

  onBillingChange(value, flag) {
    this.ordersService.getBillingZone( value)
      .subscribe({
        next: (data) => {
          if (data.length > 0) {
            this.billingStateData = data;
            if (flag == 1) {
              this.billing.zone = data[0].code;
            }
          } else {
            this.billingStateData = data;
            this.billing.zone = '';
          }
        }, error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      })
  }

  onShippingChange(value, flag) {
    this.ordersService.getBillingZone( value)
      .subscribe({
        next: (data) => {
          if (data.length > 0) {
            this.shippingStateData = data;
            if (flag == 1) {
              this.shipping.zone = data[0].code
            }
          } else {
            this.shippingStateData = data;
            this.shipping.zone = '';
          }
        }, error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      })
  }

  updateHistory() {
    this.loader = true;
    let param = {
      comments: this.statusFields.comments,
      date: moment().format('yyyy-MM-DD'),
      orderStatus: this.statusFields.status
    }
    this.ordersService.addHistory( this.orderID, param)
      .subscribe({
        next: (data) => {
          this.loader = false;
          this.toastr.success("History Status has been submitted successfully");
          this.statusFields = {
            comments: '',
            status: ''
          }
        },
        error: (err) => {
          this.loader = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      })
  }

  save() {
    this.loader = true;
    let param = {
      "emailAddress": this.info.emailAddress,
      "billing": {
        "postalCode": this.billing.postalCode,
        "firstName": this.billing.firstName,
        "lastName": this.billing.lastName,
        "company": "",
        "phone": this.billing.phone,
        "address": this.billing.address,
        "city": this.billing.city,
        "billingAddress": false,
        "zone": this.billing.zone,
        "country": this.billing.country
      },
      "delivery": {
        "postalCode": this.shipping.postalCode,
        "firstName": this.shipping.firstName,
        "lastName": this.shipping.lastName,
        "company": "",
        "phone": this.shipping.phone,
        "address": this.shipping.address,
        "city": this.shipping.city,
        "billingAddress": false,
        "zone": this.shipping.zone,
        "country": this.shipping.country
      }
    }
    this.ordersService.updateOrder( this.orderID, param)
      .subscribe({
        next: (data) => {
          this.loader = false;
          this.toastr.success("Order has been updated successfully");
        },
        error: (err) => {
          this.loader = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        },
      })
  }

  onPhoneChange() {
    this.billing.phone = new AsYouType('US').input(this.billing.phone);
  }

  onShippingPhoneChange() {
    this.shipping.phone = new AsYouType('US').input(this.shipping.phone);
  }

  goToBack() {
    this.router.navigate(['pages/orders/order-list']);
  }

  onClickRefund() {
    this.loader = true;
    this.ordersService.refundOrder(this.orderID)
      .subscribe({
        next: (data) => {
          console.log(data)
          this.loader = false;
          this.toastr.success("Order has been refunded successfully");
        },
        error: (err) => {
          this.loader = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      })
  }

  onClickCapture() {
    this.loader = true;
    this.ordersService.captureOrder( this.orderID)
      .subscribe({
        next: (data) => {
          this.loader = false;
          this.toastr.success("Order has been captured successfully");
        },
        error: (err) => {
          this.loader = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      })
  }

  showDialog(value) {
    // console.log(value)
    if (value == 1) {
      this.dialogService.open(OrderTransactionComponent, {
        context: {
          transactionData: this.transactionListData,
        },
      });
    } else if (value == 2) {
      this.dialogService.open(OrderInvoiceComponent, {
        context: {
          orderData: this.orderDetailsData,
          store: this.store
        },
      });
    } else if (value == 3) {
      this.dialogService.open(OrderHistoryComponent, {
        context: {
          historyData: this.historyListData
        },
      });
    }
  }
}
