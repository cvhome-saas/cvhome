import {Component, inject} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';
import {NbDialogRef} from '@nebular/theme';
import {ReadableOrder} from '../models/order.model';
import {Store} from '../../store-management/models/store';

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
  store: Store;

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
