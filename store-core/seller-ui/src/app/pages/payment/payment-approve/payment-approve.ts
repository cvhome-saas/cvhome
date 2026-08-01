import {Component, inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {NbButtonModule, NbDialogRef, NbFormFieldModule, NbInputModule} from '@nebular/theme';

@Component({
  selector: 'ngx-payment-approve',
  standalone: true,
  imports: [FormsModule, TranslateModule, NbButtonModule, NbFormFieldModule, NbInputModule],
  templateUrl: 'payment-approve.html',
  styleUrls: ['payment-approve.scss'],
})
export class PaymentApproveComponent {
  protected readonly ref = inject(NbDialogRef<PaymentApproveComponent>);

  transactionNo = '';

  cancel() {
    this.ref.close();
  }

  submit() {
    this.ref.close(this.transactionNo);
  }
}
