import {Component, inject} from '@angular/core';
import {NbDialogRef} from '@nebular/theme';

@Component({
  selector: 'ngx-order-invoice',
  standalone: false,
  templateUrl: 'order-invoice.html',
  styleUrls: ['order-invoice.scss'],
})
export class OrderInvoiceComponent {
  protected readonly ref = inject(NbDialogRef<OrderInvoiceComponent>);

  orderData: any;
  store: any;

  cancel() {
    this.ref.close();
  }

  submit(name?: any) {
    this.ref.close(name);
  }

  print() {
    window.print();
  }
}
