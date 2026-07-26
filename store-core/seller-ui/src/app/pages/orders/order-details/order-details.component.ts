import {Component, OnInit, inject} from '@angular/core';
import {OrderDetailsFacade} from './facades/order-details.facade';

@Component({
  selector: 'ngx-order-details',
  standalone: false,
  templateUrl: './order-details.component.html',
  styleUrls: ['./order-details.component.scss']
})
export class OrderDetailsComponent implements OnInit {
  protected readonly facade = inject(OrderDetailsFacade);

  ngOnInit() {
    this.facade.init();
  }

  get orderDetailsData() { return this.facade.orderDetailsData(); }
  get loader() { return this.facade.loader(); }
  get orderID() { return this.facade.orderID(); }
  get transactionType() { return this.facade.transactionType(); }
  get statusList() { return this.facade.statusList; }

  get billing() { return this.facade.billing(); }
  set billing(value: any) { this.facade.updateBilling(value); }

  get shipping() { return this.facade.shipping(); }
  set shipping(value: any) { this.facade.updateShipping(value); }

  get info() { return this.facade.info(); }
  set info(value: any) { this.facade.updateInfo(value); }

  get statusFields() { return this.facade.statusFields(); }
  set statusFields(value: any) { this.facade.updateStatusFields(value); }

  get shippingCountry() { return this.facade.shippingCountry(); }
  get billingCountry() { return this.facade.billingCountry(); }
  get shippingStateData() { return this.facade.shippingStateData(); }
  get billingStateData() { return this.facade.billingStateData(); }

  onBillingChange(value: any, flag: number) {
    this.facade.onBillingChange(value, flag);
  }

  onShippingChange(value: any, flag: number) {
    this.facade.onShippingChange(value, flag);
  }

  goToBack() {
    this.facade.goBack();
  }

  showDialog(value: any) {
    this.facade.showDialog(value);
  }

  onPhoneChange(phone: string) {
    this.facade.onPhoneChange(phone);
  }

  onShippingPhoneChange(phone: string) {
    this.facade.onShippingPhoneChange(phone);
  }

  updateHistory() {
    this.facade.updateHistory();
  }

  save() {
    this.facade.save();
  }

  onClickRefund() {
    this.facade.onClickRefund();
  }

  onClickCapture() {
    this.facade.onClickCapture();
  }
}
