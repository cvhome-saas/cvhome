import {Component, inject} from '@angular/core';
import {NbDialogRef} from '@nebular/theme';

@Component({
  selector: 'ngx-order-transaction',
  standalone: false,
  templateUrl: 'order-transaction.html',
  styleUrls: ['order-transaction.scss'],
})
export class OrderTransactionComponent {
  protected readonly ref = inject(NbDialogRef<OrderTransactionComponent>);

  transactionData: any;

  cancel() {
    this.ref.close();
  }

  submit(name?: any) {
    this.ref.close(name);
  }
}
