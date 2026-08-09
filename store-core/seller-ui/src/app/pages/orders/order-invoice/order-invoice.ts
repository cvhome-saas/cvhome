import {Component, inject} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';
import {NbDialogRef} from '@nebular/theme';
import {ReadableOrder} from 'seller-core/orders';
import {ReadableMerchantStore} from 'seller-core/stores';

@Component({
  selector: 'ngx-order-invoice',
  standalone: true,
  imports: [TranslateModule],
  templateUrl: 'order-invoice.html',
  styleUrls: ['order-invoice.scss'],
})
export class OrderInvoiceComponent {
  protected readonly ref = inject(NbDialogRef<OrderInvoiceComponent>);

  orderData: ReadableOrder;
  store: ReadableMerchantStore;

  cancel() {
    this.ref.close();
  }

  submit(name?: string) {
    this.ref.close(name);
  }

  print() {
    window.print();
  }
}
