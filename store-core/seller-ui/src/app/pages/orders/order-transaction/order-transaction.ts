import {Component, inject} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';
import {NbDialogRef} from '@nebular/theme';

@Component({
  selector: 'ngx-order-transaction',
  standalone: true,
  imports: [TranslateModule],
  templateUrl: 'order-transaction.html',
  styleUrls: ['order-transaction.scss'],
})
export class OrderTransactionComponent {
  protected readonly ref = inject(NbDialogRef<OrderTransactionComponent>);

  transactionData: unknown[];

  cancel() {
    this.ref.close();
  }

  submit(name?: string) {
    this.ref.close(name);
  }
}
