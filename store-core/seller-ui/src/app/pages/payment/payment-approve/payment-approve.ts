import {Component} from '@angular/core';
import {NbDialogRef} from '@nebular/theme';

@Component({
  selector: 'ngx-payment-approve',
  standalone: false,
  templateUrl: 'payment-approve.html',
  styleUrls: ['payment-approve.scss'],
})
export class PaymentApproveComponent {
  internalRef: string;
  transactionNo: string = '';

  constructor(protected ref: NbDialogRef<PaymentApproveComponent>) {
  }

  cancel() {
    this.ref.close();
  }

  submit() {
    this.ref.close(this.transactionNo);
  }
}
