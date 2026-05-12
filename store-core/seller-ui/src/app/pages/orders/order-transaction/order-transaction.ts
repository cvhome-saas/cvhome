import {Component,} from '@angular/core';
import {NbDialogRef} from '@nebular/theme';

@Component({
  selector: 'ngx-order-transaction',
  standalone: false,
  templateUrl: 'order-transaction.html',
  styleUrls: ['order-transaction.scss'],
})
export class OrderTransactionComponent {
  transactionData: any;

  constructor(protected ref: NbDialogRef<OrderTransactionComponent>) {
    // console.log(this.historyData)
  }

  cancel() {
    this.ref.close();
  }

  submit(name) {
    this.ref.close(name);
  }
}
