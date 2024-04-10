import {Component, OnInit} from '@angular/core';
import {NbDialogService, NbToastrService} from '@nebular/theme';
import {OrdersService} from '../services/orders.service';
import * as moment from 'moment';
import {OrderInvoiceComponent} from '../order-invoice/order-invoice';
import {OrderHistoryComponent} from '../order-history/order-history';
import {OrderTransactionComponent} from '../order-transaction/order-transaction';

import {ActivatedRoute, Router} from '@angular/router';
import {AsYouType} from 'libphonenumber-js';

@Component({
  selector: 'ngx-order-details',
  templateUrl: './order-details.component.html',
  styleUrls: ['./order-details.component.scss']
})
export class OrderDetailsComponent implements OnInit {

  shippingCountry: Array<any> = []
  shippingStateData: Array<any> = []
  billingStateData: Array<any> = []
  billingCountry: Array<any> = []
  groups: Array<any> = []
  selectedGroups: Array<any> = []
  loadingList = false;
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
  defaultCountry: any;
  buttonText: any = 'Update Order'
  languages: Array<any> = [{'code': 'en', 'name': 'English'}, {'code': 'fr', 'name': 'French'}]

  constructor(private ordersService: OrdersService, private toastr: NbToastrService,
              private dialogService: NbDialogService, private router: Router, private activatedRoute: ActivatedRoute) {

  }

  getOrderDetails() {
    this.loadingList = true;
    this.ordersService.getOrderDetails(this.storeID, this.orderID)
      .subscribe({
        next: (data) => {
          this.loadingList = false;
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
          this.loadingList = false;
        },
      });
  }

  ngOnInit() {
    console.log(this.activatedRoute.params.subscribe(it => {
      const ids: string[] = it["id"].split("-");
      this.storeID = ids[0];
      this.orderID = ids[1];
      this.getCountry();
      this.getOrderDetails();
      this.getHistory();
      this.getNextTransaction();
    }))
  }

  getNextTransaction() {
    this.ordersService.getNextTransaction(this.storeID, this.orderID)
      .subscribe(data => {
        // console.log(data);
        this.transactionType = data.transactionType;
      }, error => {

      });
  }

  getHistory() {
    this.ordersService.getHistory(this.storeID, this.orderID)
      .subscribe(data => {
        // console.log(data);
        this.historyListData = data;
      }, error => {

      });
    this.geTransactions()
  }

  geTransactions() {
    this.ordersService.getTransactions(this.storeID, this.orderID)
      .subscribe({
        next: (data) => {
          this.transactionListData = data;
        }
      });

  }

  getCountry() {
    this.ordersService.getCountry(this.storeID)
      .subscribe({
        next: (data) => {
          this.shippingCountry = data;
          this.billingCountry = data;
        }
      })
  }

  onBillingChange(value, flag) {
    this.ordersService.getBillingZone(this.storeID, value)
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

        }
      })
  }

  onChangeStateBilling(value) {

  }

  onShippingChange(value, flag) {

    this.ordersService.getShippingZone(this.storeID, value)
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
        }
      })
  }

  onChangeStateShipping(value) {

  }

  updateHistory() {
    this.loadingList = true;
    let param = {
      comments: this.statusFields.comments,
      date: moment().format('yyyy-MM-DD'),
      status: this.statusFields.status
    }
    this.ordersService.addHistory(this.storeID, this.orderID, param)
      .subscribe({
        next: (data) => {
          this.loadingList = false;
          this.toastr.success("History Status has been submitted successfully");
          this.statusFields = {
            comments: '',
            status: ''
          }
        },
        error: (err) => {
          this.loadingList = false;
          this.toastr.success("History Status has been submitted fail");

        }
      })
  }

  updateOrder() {
    this.loadingList = true;
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
    this.ordersService.updateOrder(this.storeID, this.orderID, param)
      .subscribe({
        next: (data) => {
          this.loadingList = false;
          this.toastr.success("Order has been updated successfully");
        },
        error: (err) => {
          this.loadingList = false;
          this.toastr.success("Order has been updated fail");
        },
      })
  }

  onPhoneChange() {
    this.billing.phone = new AsYouType('US').input(this.billing.phone);
  }

  onShippingPhoneChange() {
    this.shipping.phone = new AsYouType('US').input(this.shipping.phone);
  }

  goToback() {
    this.router.navigate(['pages/orders/order-list']);
  }

  onClickRefund() {
    this.loadingList = true;
    this.ordersService.refundOrder(this.storeID, this.orderID)
      .subscribe({
        next: (data) => {
          console.log(data)
          this.loadingList = false;
          this.toastr.success("Order has been refunded successfully");
        },
        error: (err) => {
          this.loadingList = false;
          this.toastr.danger("Order has been refunded fail");
        }
      })
  }

  onClickCapture() {
    this.loadingList = true;
    this.ordersService.captureOrder(this.storeID, this.orderID)
      .subscribe({
        next: (data) => {
          this.loadingList = false;
          this.toastr.success("Order has been captured successfully");
        },
        error: (err) => {
          this.loadingList = false;
          this.toastr.danger("Order has been captured fail");
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
